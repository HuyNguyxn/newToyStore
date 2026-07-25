package com.example.new_toy_store.payment.infrastructure.vnpay;

import com.example.new_toy_store.payment.domain.PaymentTransaction;
import com.example.new_toy_store.payment.domain.exception.InvalidPaymentDataException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class VnpayService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayProperties properties;

    public VnpayService(VnpayProperties properties) {
        this.properties = properties;
    }

    public String createPaymentUrl(PaymentTransaction payment, String clientIp) {
        validateEnabled();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", properties.getVersion());
        params.put("vnp_Command", properties.getCommand());
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(toVnpayAmount(payment.getAmount())));
        params.put("vnp_CurrCode", properties.getCurrencyCode());
        params.put("vnp_TxnRef", String.valueOf(payment.getId()));
        params.put("vnp_OrderInfo", "Pay order " + payment.getOrderId() + " with payment " + payment.getId());
        params.put("vnp_OrderType", properties.getOrderType());
        params.put("vnp_Locale", properties.getLocale());
        params.put("vnp_ReturnUrl", properties.getReturnUrl());
        params.put("vnp_IpnUrl", properties.getIpnUrl());
        params.put("vnp_IpAddr", resolveClientIp(clientIp));
        params.put("vnp_CreateDate", LocalDateTime.now(VN_ZONE).format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate", payment.getExpiredAt().atZone(VN_ZONE).toLocalDateTime().format(VNPAY_DATE_FORMAT));

        String hashData = buildQuery(params, true);
        String queryUrl = buildQuery(params, true);
        String secureHash = hmacSha512(properties.getHashSecret(), hashData);
        return properties.getPayUrl() + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;
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

        String hashData = buildQuery(signedParams, true);
        String expectedHash = hmacSha512(properties.getHashSecret(), hashData);
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    public Integer extractPaymentId(Map<String, String> params) {
        try {
            return Integer.valueOf(params.get("vnp_TxnRef"));
        } catch (NumberFormatException ex) {
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
        if (isBlank(properties.getPayUrl()) || isBlank(properties.getTmnCode())
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
        return isBlank(clientIp) ? "127.0.0.1" : clientIp;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isPlaceholder(String value) {
        return value != null && value.trim().startsWith("YOUR_VNPAY_");
    }
}
