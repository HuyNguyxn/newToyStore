import { useEffect, useState } from 'react';
import { changeReviewStatus, getAdminReviews, replyToReview } from '../../services/adminReviewService.js';
import { formatDateTime } from '../../utils/formatters.js';

function AdminReviewModerationPage() {
  const [reviews, setReviews] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [filters, setFilters] = useState({ productId: '', rating: '', status: '', hasAdminReplied: '' });
  const [selectedReview, setSelectedReview] = useState(null);
  const [reply, setReply] = useState('');
  const [status, setStatus] = useState('PUBLISHED');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadReviews(0);
  }, []);

  function normalizePage(result) {
    return {
      content: result?.content || [],
      number: result?.number || 0,
      totalPages: result?.totalPages || 1,
      totalElements: result?.totalElements || result?.content?.length || 0,
    };
  }

  function loadReviews(page = pageInfo.number) {
    setLoading(true);
    setError('');

    getAdminReviews({ ...filters, page, size: 10, sort: 'createdAt,desc' })
      .then((result) => {
        const next = normalizePage(result);
        setReviews(next.content);
        setPageInfo({ number: next.number, totalPages: next.totalPages, totalElements: next.totalElements });
      })
      .catch((err) => setError(err.message || 'Kh?ng th? t?i reviews.'))
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
      setMessage('?? g?i ph?n h?i review.');
      loadReviews(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Ph?n h?i review th?t b?i.');
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
      setMessage(`?? ??i tr?ng th?i review sang ${nextStatus}.`);
      loadReviews(pageInfo.number);
    } catch (err) {
      setError(err.message || '??i tr?ng th?i review th?t b?i.');
    } finally {
      setLoading(false);
    }
  }

  if (loading && reviews.length === 0) {
    return <div className="page-message">?ang t?i review moderation...</div>;
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin Moderation</p>
          <h2>Reviews</h2>
          <span>Moderate review visibility and reply to customer reviews.</span>
        </div>
        <strong>{pageInfo.totalElements} reviews</strong>
      </div>

      <form className="admin-filter" onSubmit={(event) => { event.preventDefault(); loadReviews(0); }}>
        {Object.keys(filters).map((field) => (
          <label key={field}>
            {field}
            <input value={filters[field]} onChange={(event) => setFilters((current) => ({ ...current, [field]: event.target.value }))} />
          </label>
        ))}
        <button type="submit">Filter</button>
      </form>

      {error && <div className="form-alert">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <div className="admin-crud-grid">
        <div className="admin-resource-table">
          <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 110px 1fr 100px 130px 150px' }}>
            <span>ID</span><span>Product</span><span>User</span><span>Rating</span><span>Status</span><span>Action</span>
          </div>
          {reviews.map((review) => (
            <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 110px 1fr 100px 130px 150px' }} key={review.id}>
              <span>{review.id}</span>
              <span>{review.productId}</span>
              <span>{review.userFullName || review.userId}</span>
              <span>{review.rating} sao</span>
              <span>{review.status}</span>
              <span className="admin-resource-table__actions"><button type="button" onClick={() => selectReview(review)}>Select</button></span>
            </div>
          ))}
        </div>

        <form className="admin-api-console" onSubmit={handleReply}>
          <div className="admin-panel__heading">
            <div>
              <p>Selected review</p>
              <h2>{selectedReview ? `Review #${selectedReview.id}` : 'Choose review'}</h2>
            </div>
          </div>

          {selectedReview && (
            <div className="review-card">
              <div className="review-card__user">
                <img src={selectedReview.userAvatar || 'https://placehold.co/48x48?text=U'} alt="Reviewer" />
                <div>
                  <strong>{selectedReview.userFullName || 'Customer'}</strong>
                  <span>{'★'.repeat(selectedReview.rating)}{'☆'.repeat(Math.max(5 - selectedReview.rating, 0))}</span>
                </div>
              </div>
              <p>{selectedReview.comment || 'No comment'} - {formatDateTime(selectedReview.createdAt)}</p>
            </div>
          )}

          <label>Status<select value={status} onChange={(event) => setStatus(event.target.value)}><option>PUBLISHED</option><option>HIDDEN</option></select></label>
          <div className="admin-resource-table__actions">
            <button type="button" disabled={!selectedReview} onClick={() => handleStatusChange(status)}>Apply status</button>
            <button type="button" className="is-danger" disabled={!selectedReview} onClick={() => handleStatusChange('HIDDEN')}>Hide</button>
            <button type="button" disabled={!selectedReview} onClick={() => handleStatusChange('PUBLISHED')}>Publish</button>
          </div>

          <label>Admin reply<textarea rows="6" value={reply} onChange={(event) => setReply(event.target.value)} maxLength="1000" /></label>
          <button type="submit" disabled={!selectedReview || loading}>Save reply</button>
        </form>
      </div>
    </section>
  );
}

export default AdminReviewModerationPage;
