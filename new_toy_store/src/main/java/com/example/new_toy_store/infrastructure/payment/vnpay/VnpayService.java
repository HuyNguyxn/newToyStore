package com.example.new_toy_store.infrastructure.payment.vnpay;

import com.example.new_toy_store.payment.domain.PaymentTransaction;
import com.example.new_toy_store.payment.domain.PaymentRefund;
import com.example.new_toy_store.payment.domain.exception.InvalidPaymentDataException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class VnpayService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public VnpayService(VnpayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String createPaymentUrl(PaymentTransaction payment, String clientIp) {
        validateEnabled();

        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        String createDate = now.format(VNPAY_DATE_FORMAT);
        String expireDate = now.plusMinutes(15).format(VNPAY_DATE_FORMAT);

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", properties.getVersion());
        params.put("vnp_Command", properties.getCommand());
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(toVnpayAmount(payment.getAmount())));
        params.put("vnp_CurrCode", properties.getCurrencyCode());
        params.put("vnp_TxnRef", payment.getId() + "_" + System.currentTimeMillis());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + payment.getOrderId());
        params.put("vnp_OrderType", properties.getOrderType());
        params.put("vnp_Locale", properties.getLocale());
        params.put("vnp_ReturnUrl", properties.getReturnUrl());
        params.put("vnp_IpAddr", resolveClientIp(clientIp));
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                hashData.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII)).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = hmacSha512(properties.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return properties.getPayUrl() + "?" + query.toString();
    }

    public boolean isValidSignature(Map<String, String> rawParams) {
        String receivedHash = rawParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> signedParams = rawParams.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("vnp_"))
                .filter(entry -> !"vnp_SecureHash".equals(entry.getKey()))
                .filter(entry -> !"vnp_SecureHashType".equals(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, TreeMap::new));

        StringBuilder hashData = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = signedParams.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                hashData.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String expectedHash = hmacSha512(properties.getHashSecret(), hashData.toString());
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    public VnpayRefundResponse requestRefund(PaymentTransaction payment, PaymentRefund refund, String adminUser, String clientIp) {
        validateEnabled();
        if (payment.getProviderTransactionId() == null || payment.getProviderTransactionId().isBlank()) {
            throw new InvalidPaymentDataException("providerTransactionId", "VNPay transaction number is required to request refund.");
        }
        if (payment.getPaidAt() == null) {
            throw new InvalidPaymentDataException("paidAt", "Original VNPay payment time is required to request refund.");
        }

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_RequestId", refund.getRefundCode());
        params.put("vnp_Version", properties.getVersion());
        params.put("vnp_Command", "refund");
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_TransactionType", refund.getAmount() >= payment.getAmount() ? "02" : "03");
        params.put("vnp_TxnRef", String.valueOf(payment.getId()));
        params.put("vnp_Amount", String.valueOf(toVnpayAmount(refund.getAmount())));
        params.put("vnp_OrderInfo", "Refund payment " + payment.getId() + " with refund " + refund.getId());
        params.put("vnp_TransactionNo", payment.getProviderTransactionId());
        params.put("vnp_TransactionDate", payment.getPaidAt().format(VNPAY_DATE_FORMAT));
        params.put("vnp_CreateBy", adminUser == null || adminUser.isBlank() ? "system" : adminUser);
        params.put("vnp_CreateDate", LocalDateTime.now(VN_ZONE).format(VNPAY_DATE_FORMAT));
        params.put("vnp_IpAddr", resolveClientIp(clientIp));
        params.put("vnp_SecureHash", hmacSha512(properties.getHashSecret(), buildRefundHashData(params)));

        try {
            String requestBody = objectMapper.writeValueAsString(params);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getRefundUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Map<String, String> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});
            String responseCode = responseBody.getOrDefault("vnp_ResponseCode", "99");
            String message = responseBody.getOrDefault("vnp_Message", "VNPay refund response code " + responseCode);
            String providerRefundId = responseBody.getOrDefault("vnp_TransactionNo", refund.getRefundCode());
            return new VnpayRefundResponse("00".equals(responseCode), providerRefundId, responseCode, message);
        } catch (Exception ex) {
            return new VnpayRefundResponse(false, null, "99", "Cannot call VNPay refund API: " + ex.getMessage());
        }
    }

    public Integer extractPaymentId(Map<String, String> params) {
        try {
            String txnRef = params.get("vnp_TxnRef");
            if (txnRef != null && txnRef.contains("_")) {
                txnRef = txnRef.split("_")[0];
            }
            return Integer.valueOf(txnRef);
        } catch (Exception ex) {
            throw new InvalidPaymentDataException("vnp_TxnRef", "VNPay transaction reference is invalid.");
        }
    }

    public long extractAmount(Map<String, String> params) {
        try {
            return Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        } catch (NumberFormatException ex) {
            throw new InvalidPaymentDataException("vnp_Amount", "VNPay amount is invalid.");
        }
    }

    public long toVnpayAmount(double amount) {
        return Math.round(Math.max(0.0, amount) * 100.0);
    }

    private void validateEnabled() {
        if (!properties.isEnabled()) {
            throw new InvalidPaymentDataException("method", "VNPay is disabled. Please enable app.payment.vnpay.enabled first.");
        }
        if (isBlank(properties.getPayUrl()) || isBlank(properties.getRefundUrl()) || isBlank(properties.getTmnCode())
                || isPlaceholder(properties.getTmnCode()) || isBlank(properties.getHashSecret())
                || isPlaceholder(properties.getHashSecret()) || isBlank(properties.getReturnUrl())
                || isBlank(properties.getIpnUrl())) {
            throw new InvalidPaymentDataException("vnpayConfig", "VNPay configuration is incomplete.");
        }
    }

    private String buildQuery(Map<String, String> params, boolean encoded) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .map(entry -> encode(entry.getKey(), encoded) + "=" + encode(entry.getValue(), encoded))
                .collect(Collectors.joining("&"));
    }

    private String buildRefundHashData(Map<String, String> params) {
        return String.join("|",
                params.getOrDefault("vnp_RequestId", ""),
                params.getOrDefault("vnp_Version", ""),
                params.getOrDefault("vnp_Command", ""),
                params.getOrDefault("vnp_TmnCode", ""),
                params.getOrDefault("vnp_TransactionType", ""),
                params.getOrDefault("vnp_TxnRef", ""),
                params.getOrDefault("vnp_Amount", ""),
                params.getOrDefault("vnp_TransactionNo", ""),
                params.getOrDefault("vnp_TransactionDate", ""),
                params.getOrDefault("vnp_CreateBy", ""),
                params.getOrDefault("vnp_CreateDate", ""),
                params.getOrDefault("vnp_IpAddr", ""),
                params.getOrDefault("vnp_OrderInfo", "")
        );
    }

    private String encode(String value, boolean encoded) {
        if (!encoded) return value;
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new InvalidPaymentDataException("secureHash", "Cannot create VNPay secure hash.");
        }
    }

    private String resolveClientIp(String clientIp) {
        if (isBlank(clientIp) || clientIp.contains(":") || clientIp.equals("0:0:0:0:0:0:0:1") || clientIp.equals("::1")) {
            return "127.0.0.1";
        }
        return clientIp;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isPlaceholder(String value) {
        return value != null && value.trim().startsWith("YOUR_VNPAY_");
    }
}
