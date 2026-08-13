import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import { createCustomerReturn } from '../../services/customerReturnService.js';
import { getMyOrders } from '../../services/orderService.js';
import { uploadImage } from '../../services/uploadService.js';
import { formatPrice } from '../../utils/formatters.js';

const returnReasons = [
  { code: 'CHANGED_MIND', label: 'Đổi ý / Không còn nhu cầu sử dụng 🔄' },
  { code: 'DEFECTIVE', label: 'Sản phẩm bị lỗi / Hỏng do nhà sản xuất 💔' },
  { code: 'WRONG_ITEM', label: 'Giao sai sản phẩm / Khác mẫu mã đã đặt ❌' },
  { code: 'DAMAGED_IN_TRANSIT', label: 'Bưu kiện hư hỏng do quá trình vận chuyển 🚚' },
];

function getOrderStatusCode(status) {
  if (!status) return '';
  if (typeof status === 'string') return status.toUpperCase();
  return String(status.code || status.name || status.value || '').toUpperCase();
}

function ReturnCreatePage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const urlOrderId = searchParams.get('orderId') || '';

  const [myOrders, setMyOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [selectedOrderId, setSelectedOrderId] = useState(urlOrderId);

  // Form State
  const [reasonNote, setReasonNote] = useState('');
  const [proofImages, setProofImages] = useState([]);
  // Selected return items mapping: { orderItemId: { checked: boolean, qty: number, reasonCode: string, item: obj } }
  const [selectedItemsMap, setSelectedItemsMap] = useState({});

  const [loadingOrders, setLoadingOrders] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadOrders();
  }, []);

  async function loadOrders() {
    setLoadingOrders(true);
    try {
      const res = await getMyOrders({ page: 0, size: 50 });
      const list = res.content || (Array.isArray(res) ? res : []);
      // Filter for delivered / completed orders
      const eligible = list.filter((o) => {
        const s = getOrderStatusCode(o.status);
        return s.includes('COMPLETED') || s.includes('DELIVERED') || s.includes('PAID') || s.includes('SHIPPED');
      });
      setMyOrders(eligible.length > 0 ? eligible : list);

      if (urlOrderId) {
        const found = list.find((o) => String(o.id) === String(urlOrderId));
        if (found) {
          selectOrderObj(found);
        }
      } else if (eligible.length > 0) {
        selectOrderObj(eligible[0]);
      } else if (list.length > 0) {
        selectOrderObj(list[0]);
      }
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách đơn hàng để hoàn trả.');
    } finally {
      setLoadingOrders(false);
    }
  }

  function selectOrderObj(order) {
    setSelectedOrder(order);
    setSelectedOrderId(String(order.id));

    // Initialize item selection map
    const newMap = {};
    if (Array.isArray(order.items)) {
      order.items.forEach((it) => {
        newMap[it.id] = {
          checked: false,
          qty: 1,
          maxQty: it.quantity || 1,
          reasonCode: 'CHANGED_MIND',
          unitPrice: it.price || it.unitPrice || 0,
          item: it,
        };
      });
    }
    setSelectedItemsMap(newMap);
  }

  function handleOrderSelectChange(orderId) {
    setSelectedOrderId(orderId);
    const found = myOrders.find((o) => String(o.id) === String(orderId));
    if (found) {
      selectOrderObj(found);
    }
  }

  function toggleItemCheck(orderItemId) {
    setSelectedItemsMap((prev) => ({
      ...prev,
      [orderItemId]: {
        ...prev[orderItemId],
        checked: !prev[orderItemId]?.checked,
      },
    }));
  }

  function updateItemQty(orderItemId, qty) {
    setSelectedItemsMap((prev) => ({
      ...prev,
      [orderItemId]: {
        ...prev[orderItemId],
        qty: Math.max(1, Math.min(qty, prev[orderItemId]?.maxQty || 1)),
      },
    }));
  }

  function updateItemReason(orderItemId, reasonCode) {
    setSelectedItemsMap((prev) => ({
      ...prev,
      [orderItemId]: {
        ...prev[orderItemId],
        reasonCode,
      },
    }));
  }

  async function handleProofUpload(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setError('');

    try {
      const result = await uploadImage(file, 'returns');
      const url = result.secureUrl || result.url;
      setProofImages((prev) => [...prev, url]);
    } catch (err) {
      setError(err.message || 'Upload ảnh minh chứng thất bại. Vui lòng thử lại.');
    } finally {
      setUploading(false);
      event.target.value = '';
    }
  }

  function removeProofImage(index) {
    setProofImages((prev) => prev.filter((_, i) => i !== index));
  }

  // Compute Total Expected Refund
  const totalExpectedRefund = Object.values(selectedItemsMap)
    .filter((entry) => entry.checked)
    .reduce((sum, entry) => sum + entry.qty * entry.unitPrice, 0);

  async function submitReturn(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (!selectedOrderId) {
      setError('Vui lòng chọn đơn hàng cần đổi trả.');
      return;
    }

    const itemsToReturn = Object.entries(selectedItemsMap)
      .filter(([_, entry]) => entry.checked)
      .map(([orderItemId, entry]) => ({
        orderItemId: Number(orderItemId),
        productId: Number(entry.item.productId),
        variantId: entry.item.variantId ? Number(entry.item.variantId) : null,
        quantity: Number(entry.qty),
        reasonCode: entry.reasonCode,
        expectedRefundAmount: Number(entry.qty * entry.unitPrice),
      }));

    if (itemsToReturn.length === 0) {
      setError('Vui lòng chọn ít nhất 1 sản phẩm trong đơn hàng để đổi trả.');
      return;
    }

    setSubmitting(true);
    try {
      await createCustomerReturn({
        orderId: Number(selectedOrderId),
        reasonNote: reasonNote.trim() || null,
        proofImageUrls: proofImages,
        items: itemsToReturn,
      });

      setMessage('🎉 Đã gửi yêu cầu đổi trả thành công. Bộ phận CSKH sẽ hỗ trợ bạn sớm nhất!');
      setTimeout(() => {
        navigate('/returns');
      }, 1500);
    } catch (err) {
      setError(err.message || 'Gửi yêu cầu đổi trả thất bại. Vui lòng thử lại.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="container" style={{ padding: '24px 16px', maxWidth: '850px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <BackLink fallback="/returns" label="Quay lại danh sách yêu cầu" />

      {/* HERO BANNER */}
      <div
        style={{
          background: 'linear-gradient(135deg, #ffffff 0%, #fff7ed 50%, #ffedd5 100%)',
          borderRadius: '20px',
          padding: '28px 32px',
          color: '#0f172a',
          marginBottom: '24px',
          border: '1px solid #fed7aa',
          boxShadow: '0 10px 25px rgba(234, 88, 12, 0.08)',
          marginTop: '12px',
        }}
      >
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: '#ffedd5', color: '#ea580c', padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.6px', marginBottom: '10px' }}>
          🔄 ĐỔI TRẢ & HOÀN TIỀN
        </div>
        <h1 style={{ fontSize: '26px', fontWeight: '900', color: '#0f172a', margin: '0 0 6px 0', letterSpacing: '-0.5px' }}>
          Tạo yêu cầu Đổi / Trả hàng
        </h1>
        <p style={{ margin: 0, color: '#475569', fontSize: '14px', fontWeight: '500' }}>
          Hỗ trợ hoàn trả sản phẩm không ưng ý hoặc bị lỗi trong vòng 7 ngày kể từ khi nhận hàng
        </p>
      </div>

      {error && (
        <div style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '14px 18px', borderRadius: '12px', marginBottom: '20px', fontWeight: '600', fontSize: '14px' }}>
          ⚠️ {error}
        </div>
      )}

      {message && (
        <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '14px 18px', borderRadius: '12px', marginBottom: '20px', fontWeight: '700', fontSize: '14px' }}>
          {message}
        </div>
      )}

      <form onSubmit={submitReturn} style={{ background: '#ffffff', borderRadius: '20px', padding: '28px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        
        {/* STEP 1: CHỌN ĐƠN HÀNG */}
        <div>
          <label style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
            1. Chọn đơn hàng cần đổi trả <span style={{ color: '#ea580c' }}>*</span>
          </label>

          {loadingOrders ? (
            <div style={{ padding: '14px', background: '#f8fafc', borderRadius: '12px', color: '#94a3b8', fontSize: '13px' }}>
              Đang tải danh sách đơn hàng...
            </div>
          ) : myOrders.length > 0 ? (
            <select
              value={selectedOrderId}
              onChange={(e) => handleOrderSelectChange(e.target.value)}
              style={{
                width: '100%',
                padding: '12px 16px',
                fontSize: '14px',
                fontWeight: '700',
                color: '#0f172a',
                borderRadius: '12px',
                border: '1px solid #cbd5e1',
                background: '#ffffff',
                cursor: 'pointer',
              }}
            >
              {myOrders.map((o) => (
                <option key={o.id} value={o.id}>
                  Đơn hàng #DH{o.id} - Tổng tiền: {formatPrice(o.totalAmount || o.finalAmount || 0)} ({o.createdAt ? new Date(o.createdAt).toLocaleDateString('vi-VN') : ''})
                </option>
              ))}
            </select>
          ) : (
            <div style={{ padding: '14px', background: '#fff7ed', border: '1px solid #fed7aa', borderRadius: '12px', color: '#c2410c', fontSize: '13px', fontWeight: '600' }}>
              ℹ️ Vui lòng nhập Mã đơn hàng bên dưới nếu không thấy trong danh sách.
              <input
                type="number"
                value={selectedOrderId}
                onChange={(e) => setSelectedOrderId(e.target.value)}
                placeholder="Nhập Order ID"
                style={{ width: '100%', marginTop: '8px', padding: '10px', borderRadius: '8px', border: '1px solid #cbd5e1' }}
              />
            </div>
          )}
        </div>

        {/* STEP 2: CHỌN SẢN PHẨM CẦN ĐỔI TRẢ TRONG ĐƠN */}
        {selectedOrder && (
          <div>
            <label style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
              2. Đánh dấu các sản phẩm muốn hoàn trả <span style={{ color: '#ea580c' }}>*</span>
            </label>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {selectedOrder.items && selectedOrder.items.length > 0 ? (
                selectedOrder.items.map((it) => {
                  const mapEntry = selectedItemsMap[it.id] || {};
                  const isChecked = !!mapEntry.checked;

                  return (
                    <div
                      key={it.id}
                      style={{
                        padding: '16px',
                        borderRadius: '16px',
                        background: isChecked ? '#fff7ed' : '#ffffff',
                        border: isChecked ? '2px solid #ea580c' : '1px solid #e2e8f0',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '12px',
                        transition: 'all 0.15s ease',
                      }}
                    >
                      {/* CHECKBOX & ITEM HEADER */}
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <input
                          type="checkbox"
                          checked={isChecked}
                          onChange={() => toggleItemCheck(it.id)}
                          style={{ width: '20px', height: '20px', accentColor: '#ea580c', cursor: 'pointer' }}
                        />

                        {it.productImage || it.imageUrl ? (
                          <img src={it.productImage || it.imageUrl} alt="" style={{ width: '48px', height: '48px', borderRadius: '10px', objectFit: 'cover' }} />
                        ) : (
                          <div style={{ width: '48px', height: '48px', borderRadius: '10px', background: '#ffedd5', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px' }}>🎁</div>
                        )}

                        <div style={{ flex: 1 }}>
                          <h4 style={{ margin: '0 0 2px 0', fontSize: '14.5px', fontWeight: '800', color: '#0f172a' }}>
                            {it.productName || `Sản phẩm #${it.productId}`}
                          </h4>
                          <span style={{ fontSize: '12.5px', color: '#64748b', fontWeight: '600' }}>
                            Đơn giá: <strong style={{ color: '#ea580c' }}>{formatPrice(it.price || it.unitPrice || 0)}</strong> · Đã mua: {it.quantity || 1}
                          </span>
                        </div>
                      </div>

                      {/* EXTRA RETURN SETTINGS (IF CHECKED) */}
                      {isChecked && (
                        <div style={{ padding: '12px', background: '#ffffff', borderRadius: '12px', border: '1px solid #fed7aa', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                          <div>
                            <label style={{ fontSize: '12px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '4px' }}>
                              Số lượng trả (Tối đa {mapEntry.maxQty})
                            </label>
                            <input
                              type="number"
                              min="1"
                              max={mapEntry.maxQty}
                              value={mapEntry.qty}
                              onChange={(e) => updateItemQty(it.id, Number(e.target.value))}
                              style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '13.5px', fontWeight: '700' }}
                            />
                          </div>

                          <div>
                            <label style={{ fontSize: '12px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '4px' }}>
                              Lý do trả sản phẩm này
                            </label>
                            <select
                              value={mapEntry.reasonCode}
                              onChange={(e) => updateItemReason(it.id, e.target.value)}
                              style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '12.5px', fontWeight: '700' }}
                            >
                              {returnReasons.map((r) => (
                                <option key={r.code} value={r.code}>{r.label}</option>
                              ))}
                            </select>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })
              ) : (
                <div style={{ padding: '14px', background: '#f8fafc', borderRadius: '12px', color: '#64748b', fontSize: '13px' }}>
                  Đơn hàng này chưa có thông tin sản phẩm chi tiết.
                </div>
              )}
            </div>
          </div>
        )}

        {/* STEP 3: LÝ DO CHI TIẾT */}
        <div>
          <label style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
            3. Mô tả lý do chi tiết & ghi chú gửi Shop
          </label>
          <textarea
            rows="4"
            value={reasonNote}
            onChange={(e) => setReasonNote(e.target.value)}
            placeholder="Mô tả cụ thể tình trạng hàng hóa (ví dụ: bị bóp méo khi vận chuyển, hỏng đèn/tiếng kêu, sai kích thước...)..."
            style={{
              width: '100%',
              padding: '14px 16px',
              fontSize: '14px',
              fontWeight: '500',
              color: '#0f172a',
              borderRadius: '14px',
              border: '1px solid #cbd5e1',
              fontFamily: 'inherit',
              lineHeight: '1.5',
              boxSizing: 'border-box',
            }}
          />
        </div>

        {/* STEP 4: UPLOAD ẢNH MINH CHỨNG */}
        <div>
          <label style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
            4. Tải lên ảnh minh chứng thực tế (tùy chọn nhưng khuyến khích) 📸
          </label>

          <label style={{ cursor: uploading ? 'not-allowed' : 'pointer', display: 'inline-block' }}>
            <input
              type="file"
              accept="image/*"
              onChange={handleProofUpload}
              disabled={uploading}
              style={{ display: 'none' }}
            />
            <div style={{ padding: '10px 18px', borderRadius: '12px', background: '#fff7ed', border: '1px solid #fed7aa', color: '#ea580c', fontSize: '13px', fontWeight: '800', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
              📸 Tải ảnh minh chứng (lỗi/hỏng)
            </div>
          </label>

          {uploading && <span style={{ fontSize: '12.5px', color: '#ea580c', fontWeight: '700', marginLeft: '10px' }}>⏳ Đang tải ảnh...</span>}

          {/* PROOF THUMBNAILS GRID */}
          {proofImages.length > 0 && (
            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginTop: '12px' }}>
              {proofImages.map((imgUrl, idx) => (
                <div key={idx} style={{ position: 'relative', width: '80px', height: '80px', borderRadius: '12px', overflow: 'hidden', border: '1px solid #cbd5e1' }}>
                  <img src={imgUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  <button
                    type="button"
                    onClick={() => removeProofImage(idx)}
                    style={{ position: 'absolute', top: '4px', right: '4px', background: 'rgba(0,0,0,0.6)', color: '#fff', border: 'none', borderRadius: '50%', width: '20px', height: '20px', cursor: 'pointer', fontSize: '11px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ESTIMATED REFUND SUMMARY CARD */}
        <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', padding: '18px 22px', borderRadius: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <span style={{ fontSize: '12.5px', fontWeight: '800', color: '#15803d', textTransform: 'uppercase' }}>TỔNG TIỀN DỰ KIẾN HOÀN TRẢ</span>
            <div style={{ fontSize: '22px', fontWeight: '900', color: '#16a34a' }}>
              {formatPrice(totalExpectedRefund)}
            </div>
          </div>
          <span style={{ fontSize: '12px', color: '#166534', fontWeight: '600', maxWidth: '240px', textAlign: 'right' }}>
            Số tiền thực tế hoàn trả sẽ được nhân viên shop xác nhận sau khi nhận lại sản phẩm.
          </span>
        </div>

        {/* SUBMIT BUTTON */}
        <button
          type="submit"
          disabled={submitting || uploading}
          style={{
            padding: '16px 24px',
            fontSize: '16px',
            fontWeight: '900',
            color: '#ffffff',
            background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)',
            border: 'none',
            borderRadius: '14px',
            cursor: submitting || uploading ? 'not-allowed' : 'pointer',
            boxShadow: '0 6px 20px rgba(234, 88, 12, 0.3)',
            transition: 'all 0.2s ease',
          }}
        >
          {submitting ? '⏳ Đang gửi yêu cầu...' : '🔄 Gửi yêu cầu Đổi / Trả hàng ngay'}
        </button>
      </form>
    </div>
  );
}

export default ReturnCreatePage;
