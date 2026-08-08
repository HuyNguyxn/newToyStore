import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { changeReviewStatus, getAdminReviews, replyToReview } from '../../services/adminReviewService.js';
import { formatDateTime } from '../../utils/formatters.js';

// Status badge helper
function getReviewStatusInfo(status) {
  const statusStr = String(status || '').toUpperCase();
  if (statusStr === 'PUBLISHED') {
    return { label: 'Hiển thị công khai', bg: '#d1fae5', color: '#10b981', border: '#a7f3d0' };
  }
  if (statusStr === 'HIDDEN') {
    return { label: 'Ẩn', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
  }
  return { label: statusStr, bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
}

function AdminReviewModerationPage() {
  const [searchParams] = useSearchParams();
  const initialRating = searchParams.get('rating') || '';

  const [reviews, setReviews] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [filters, setFilters] = useState({ productId: '', rating: initialRating, status: '', hasAdminReplied: '' });
  const [selectedReview, setSelectedReview] = useState(null);
  const [reply, setReply] = useState('');
  const [status, setStatus] = useState('PUBLISHED');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadReviews(0, initialRating);
  }, []);

  function normalizePage(result) {
    return {
      content: result?.content || result || [],
      number: result?.number || 0,
      totalPages: result?.totalPages || 1,
      totalElements: result?.totalElements || result?.content?.length || result?.length || 0,
    };
  }

  function loadReviews(page = pageInfo.number, overrideRating) {
    setLoading(true);
    setError('');
    const targetRating = overrideRating !== undefined ? overrideRating : filters.rating;

    getAdminReviews({
      productId: filters.productId || undefined,
      rating: targetRating ? Number(targetRating) : undefined,
      status: filters.status || undefined,
      hasAdminReplied: filters.hasAdminReplied === '' ? undefined : (filters.hasAdminReplied === 'true'),
      page,
      size: 10,
      sort: 'createdAt,desc',
    })
      .then((result) => {
        const next = normalizePage(result);
        setReviews(next.content);
        setPageInfo({ number: next.number, totalPages: next.totalPages, totalElements: next.totalElements });
      })
      .catch((err) => setError(err.message || 'Không thể tải danh sách đánh giá.'))
      .finally(() => setLoading(false));
  }

  function selectReview(review) {
    setSelectedReview(review);
    setReply(review.adminReply || '');
    setStatus(review.status || 'PUBLISHED');
  }

  async function handleReply(event) {
    event.preventDefault();
    if (!selectedReview) return;
    setError('');
    setMessage('');
    setLoading(true);
    try {
      await replyToReview(selectedReview.id, reply.trim());
      setMessage('Đã gửi phản hồi đánh giá.');
      loadReviews(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Gửi phản hồi đánh giá thất bại.');
    } finally {
      setLoading(false);
    }
  }

  async function handleStatusChange(nextStatus = status) {
    if (!selectedReview) return;
    setError('');
    setMessage('');
    setLoading(true);
    try {
      await changeReviewStatus(selectedReview.id, nextStatus);
      setStatus(nextStatus);
      setMessage(`Đã cập nhật trạng thái hiển thị đánh giá sang: ${getReviewStatusInfo(nextStatus).label}.`);
      loadReviews(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Thay đổi trạng thái thất bại.');
    } finally {
      setLoading(false);
    }
  }

  const handleClearFilters = () => {
    setFilters({ productId: '', rating: '', status: '', hasAdminReplied: '' });
    setTimeout(() => {
      loadReviews(0);
    }, 50);
  };

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Kiểm duyệt đánh giá của khách hàng
        </h1>
        <div style={{ background: '#fff7ed', color: '#ea580c', border: '1px solid #ffedd5', padding: '6px 16px', borderRadius: '20px', fontSize: '13px', fontWeight: '700' }}>
          Đánh giá: {pageInfo.totalElements}
        </div>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* FILTER BAR */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          loadReviews(0);
        }}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '16px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '160px' }}>
          <input
            type="text"
            placeholder="Mã sản phẩm (Product ID)..."
            value={filters.productId}
            onChange={(e) => setFilters({ ...filters, productId: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '130px' }}>
          <select
            value={filters.rating}
            onChange={(e) => setFilters({ ...filters, rating: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả số sao</option>
            <option value="5">5 sao</option>
            <option value="4">4 sao</option>
            <option value="3">3 sao</option>
            <option value="2">2 sao</option>
            <option value="1">1 sao</option>
          </select>
        </div>

        <div style={{ flex: '1', minWidth: '160px' }}>
          <select
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="PUBLISHED">Công khai (PUBLISHED)</option>
            <option value="HIDDEN">Ẩn hiển thị (HIDDEN)</option>
          </select>
        </div>

        <div style={{ flex: '1', minWidth: '160px' }}>
          <select
            value={filters.hasAdminReplied}
            onChange={(e) => setFilters({ ...filters, hasAdminReplied: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tình trạng phản hồi</option>
            <option value="true">Đã phản hồi</option>
            <option value="false">Chưa phản hồi</option>
          </select>
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <button
            type="submit"
            style={{ padding: '9px 20px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Lọc
          </button>
          <button
            type="button"
            onClick={handleClearFilters}
            style={{ padding: '9px 14px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Xóa lọc
          </button>
        </div>
      </form>

      {/* MAIN SPLIT GRID */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '20px', alignItems: 'start' }}>
        
        {/* Table List */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'visible' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                <th style={{ padding: '14px 16px', width: '60px' }}>ID</th>
                <th style={{ padding: '14px 16px', width: '90px' }}>Sản phẩm</th>
                <th style={{ padding: '14px 16px' }}>Khách hàng</th>
                <th style={{ padding: '14px 16px', width: '100px' }}>Đánh giá</th>
                <th style={{ padding: '14px 16px', width: '130px' }}>Trạng thái</th>
                <th style={{ padding: '14px 16px', width: '100px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading && reviews.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
                    Đang tải danh sách đánh giá...
                  </td>
                </tr>
              ) : reviews.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                    Không tìm thấy đánh giá nào.
                  </td>
                </tr>
              ) : (
                reviews.map((review) => {
                  const statusInfo = getReviewStatusInfo(review.status);
                  return (
                    <tr
                      key={review.id}
                      style={{
                        borderBottom: '1px solid #f1f5f9',
                      }}
                    >
                      <td style={{ padding: '14px 16px', fontWeight: '600', color: '#334155' }}>
                        #{review.id}
                      </td>
                      <td style={{ padding: '14px 16px', fontWeight: '600', color: '#ea580c' }}>
                        SP{review.productId}
                      </td>
                      <td style={{ padding: '14px 16px', color: '#334155', fontWeight: '700' }}>
                        {review.userFullName || `User #${review.userId}`}
                      </td>
                      <td style={{ padding: '14px 16px', color: '#eab308', fontWeight: 'bold' }}>
                        {review.rating} ★
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <span
                          style={{
                            background: statusInfo.bg,
                            color: statusInfo.color,
                            border: `1px solid ${statusInfo.border}`,
                            padding: '3px 8px',
                            borderRadius: '6px',
                            fontSize: '11.5px',
                            fontWeight: '700',
                          }}
                        >
                          {statusInfo.label}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                        <button
                          type="button"
                          onClick={() => selectReview(review)}
                          style={{
                            padding: '6px 12px',
                            background: '#ffffff',
                            color: '#475569',
                            border: '1px solid #cbd5e1',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: '700',
                          }}
                        >
                          Xem
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>

          {/* Pagination */}
          {pageInfo.totalPages > 1 && (
            <div style={{ display: 'flex', gap: '8px', padding: '16px', justifyContent: 'flex-end', borderTop: '1px solid #f1f5f9' }}>
              <button
                disabled={pageInfo.number === 0}
                onClick={() => loadReviews(pageInfo.number - 1)}
                style={{ padding: '6px 12px', background: '#f1f5f9', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' }}
              >
                Trước
              </button>
              <span style={{ fontSize: '13px', alignSelf: 'center', color: '#475569' }}>
                Trang {pageInfo.number + 1} / {pageInfo.totalPages}
              </span>
              <button
                disabled={pageInfo.number === pageInfo.totalPages - 1}
                onClick={() => loadReviews(pageInfo.number + 1)}
                style={{ padding: '6px 12px', background: '#f1f5f9', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' }}
              >
                Sau
              </button>
            </div>
          )}
        </div>

        {/* Right Detail & Reply Form */}
        <aside style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
            <h3 style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', margin: 0 }}>
              {selectedReview ? `Đánh giá #${selectedReview.id}` : 'Chi tiết đánh giá'}
            </h3>
            {selectedReview && (
              <button
                type="button"
                onClick={() => setSelectedReview(null)}
                style={{ border: 'none', background: '#f1f5f9', borderRadius: '6px', width: '26px', height: '26px', cursor: 'pointer', fontWeight: '700' }}
              >
                ✕
              </button>
            )}
          </div>

          {selectedReview ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              
              {/* Review content card */}
              <div style={{ background: '#f8fafc', padding: '14px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '13px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px' }}>
                  <img
                    src={selectedReview.userAvatar || 'https://placehold.co/48x48?text=U'}
                    alt="Reviewer"
                    style={{ width: '36px', height: '36px', borderRadius: '50%', objectFit: 'cover' }}
                  />
                  <div>
                    <strong style={{ display: 'block', color: '#0f172a' }}>{selectedReview.userFullName || 'Khách hàng'}</strong>
                    <span style={{ color: '#eab308', fontSize: '12px' }}>
                      {'★'.repeat(selectedReview.rating)}{'☆'.repeat(Math.max(5 - selectedReview.rating, 0))}
                    </span>
                  </div>
                </div>
                <p style={{ color: '#334155', margin: '0 0 6px 0', lineHeight: 1.4, fontStyle: 'italic' }}>
                  "{selectedReview.comment || 'Không có bình luận'}"
                </p>
                <span style={{ fontSize: '11px', color: '#94a3b8' }}>
                  Ngày tạo: {formatDateTime(selectedReview.createdAt)}
                </span>
              </div>

              {/* Status configuration */}
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Trạng thái hiển thị</label>
                <select
                  value={status}
                  onChange={(event) => setStatus(event.target.value)}
                  style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff', marginBottom: '10px' }}
                >
                  <option value="PUBLISHED">Công khai (PUBLISHED)</option>
                  <option value="HIDDEN">Ẩn hiển thị (HIDDEN)</option>
                </select>

                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    type="button"
                    onClick={() => handleStatusChange('PUBLISHED')}
                    style={{ flex: 1, padding: '9px', background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                  >
                    Hiện công khai
                  </button>
                  <button
                    type="button"
                    onClick={() => handleStatusChange('HIDDEN')}
                    style={{ flex: 1, padding: '9px', background: '#ffffff', color: '#dc2626', border: '1px solid #fecaca', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                  >
                    Ẩn đi
                  </button>
                </div>
              </div>

              {/* Reply form */}
              <form onSubmit={handleReply} style={{ borderTop: '1px solid #f1f5f9', paddingTop: '14px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Phản hồi của Admin</label>
                  <textarea
                    rows="4"
                    value={reply}
                    onChange={(event) => setReply(event.target.value)}
                    maxLength="1000"
                    placeholder="Nhập nội dung phản hồi của cửa hàng..."
                    style={{ width: '100%', padding: '10px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '12.5px', outline: 'none', resize: 'none' }}
                  />
                </div>
                <button
                  type="submit"
                  disabled={loading}
                  style={{ width: '100%', padding: '10px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
                >
                  {loading ? 'Đang lưu...' : 'Lưu phản hồi'}
                </button>
              </form>

            </div>
          ) : (
            <div style={{ textAlign: 'center', color: '#94a3b8', fontSize: '13px', padding: '24px 0' }}>
              Vui lòng chọn 1 đánh giá bên trái để xem nội dung chi tiết và phản hồi khách hàng.
            </div>
          )}

        </aside>

      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminReviewModerationPage;
