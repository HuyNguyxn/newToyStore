import { useEffect, useState } from 'react';
import {
  activatePromotion,
  createPromotion,
  deactivatePromotion,
  deletePromotion,
  getAdminPromotions,
  updatePromotion,
} from '../../services/adminPromotionService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

const emptyPromotionForm = {
  id: '',
  code: '',
  name: '',
  type: 'PERCENTAGE',
  scope: 'ORDER',
  discountValue: '',
  maxDiscountAmount: '',
  minOrderValue: '',
  targetProductId: '',
  usageLimit: '',
  startDate: '',
  endDate: '',
};

function AdminPromotionPage() {
  const [promotions, setPromotions] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [filters, setFilters] = useState({ keyword: '', scope: '', active: '' });
  const [form, setForm] = useState(emptyPromotionForm);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadPromotions(0);
  }, []);

  function normalizePage(result) {
    return {
      content: result?.content || [],
      number: result?.number || 0,
      totalPages: result?.totalPages || 1,
      totalElements: result?.totalElements || result?.content?.length || 0,
    };
  }

  function loadPromotions(page = pageInfo.number) {
    setLoading(true);
    setError('');
    getAdminPromotions({ ...filters, page, size: 10 })
      .then((result) => {
        const next = normalizePage(result);
        setPromotions(next.content);
        setPageInfo({ number: next.number, totalPages: next.totalPages, totalElements: next.totalElements });
      })
      .catch((err) => setError(err.message || 'Kh?ng th? t?i khuy?n m?i.'))
      .finally(() => setLoading(false));
  }

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function selectPromotion(promotion) {
    setForm({
      id: promotion.id || '',
      code: promotion.code || '',
      name: promotion.name || '',
      type: promotion.type || 'PERCENTAGE',
      scope: promotion.scope || 'ORDER',
      discountValue: promotion.discountValue ?? '',
      maxDiscountAmount: promotion.maxDiscountAmount ?? '',
      minOrderValue: promotion.minOrderValue ?? '',
      targetProductId: promotion.targetProductId ?? '',
      usageLimit: promotion.usageLimit ?? '',
      startDate: toDateTimeInput(promotion.startDate),
      endDate: toDateTimeInput(promotion.endDate),
    });
  }

  function buildPayload() {
    return {
      code: form.code.trim(),
      name: form.name.trim(),
      type: form.type,
      scope: form.scope,
      discountValue: Number(form.discountValue || 0),
      maxDiscountAmount: form.maxDiscountAmount === '' ? null : Number(form.maxDiscountAmount),
      minOrderValue: form.minOrderValue === '' ? null : Number(form.minOrderValue),
      targetProductId: form.targetProductId === '' ? null : Number(form.targetProductId),
      usageLimit: form.usageLimit === '' ? null : Number(form.usageLimit),
      startDate: form.startDate ? `${form.startDate}:00` : null,
      endDate: form.endDate ? `${form.endDate}:00` : null,
    };
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    setMessage('');
    try {
      if (form.id) {
        await updatePromotion(form.id, buildPayload());
        setMessage('?? c?p nh?t khuy?n m?i.');
      } else {
        await createPromotion(buildPayload());
        setMessage('?? t?o khuy?n m?i.');
      }
      setForm(emptyPromotionForm);
      loadPromotions(0);
    } catch (err) {
      setError(err.message || 'L?u khuy?n m?i th?t b?i.');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAction(action) {
    setSubmitting(true);
    setError('');
    setMessage('');
    try {
      await action();
      setMessage('?? c?p nh?t khuy?n m?i.');
      loadPromotions(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Thao t?c khuy?n m?i th?t b?i.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="page-message">?ang t?i khuy?n m?i admin...</div>;
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin CRUD</p>
          <h2>Promotions</h2>
          <span>Create, update, activate, deactivate, and delete promotion campaigns.</span>
        </div>
        <strong>{pageInfo.totalElements} promotions</strong>
      </div>

      <form className="admin-filter" onSubmit={(event) => { event.preventDefault(); loadPromotions(0); }}>
        {['keyword', 'scope', 'active'].map((field) => (
          <label key={field}>
            {field}
            <input value={filters[field]} onChange={(event) => setFilters((current) => ({ ...current, [field]: event.target.value }))} />
          </label>
        ))}
        <button type="submit">Filter</button>
      </form>

      {error && <div className="form-alert">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <form className="admin-api-console" onSubmit={handleSubmit}>
        <div className="admin-panel__heading">
          <div>
            <p>{form.id ? `Edit #${form.id}` : 'Create'}</p>
            <h2>Promotion form</h2>
          </div>
          {form.id && <button type="button" onClick={() => setForm(emptyPromotionForm)}>New</button>}
        </div>
        <div className="admin-api-console__row">
          <label>Code<input value={form.code} onChange={(event) => updateForm('code', event.target.value.toUpperCase())} required /></label>
          <label>Name<input value={form.name} onChange={(event) => updateForm('name', event.target.value)} required /></label>
        </div>
        <div className="admin-api-console__row">
          <label>Type<select value={form.type} onChange={(event) => updateForm('type', event.target.value)}><option>PERCENTAGE</option><option>FIXED_AMOUNT</option><option>FREE_SHIPPING</option></select></label>
          <label>Scope<select value={form.scope} onChange={(event) => updateForm('scope', event.target.value)}><option>ORDER</option><option>PRODUCT</option><option>SHIPPING</option></select></label>
        </div>
        <div className="admin-api-console__row">
          <label>Discount value<input type="number" value={form.discountValue} onChange={(event) => updateForm('discountValue', event.target.value)} required /></label>
          <label>Max discount<input type="number" value={form.maxDiscountAmount} onChange={(event) => updateForm('maxDiscountAmount', event.target.value)} /></label>
        </div>
        <div className="admin-api-console__row">
          <label>Min order<input type="number" value={form.minOrderValue} onChange={(event) => updateForm('minOrderValue', event.target.value)} /></label>
          <label>Target product ID<input type="number" value={form.targetProductId} onChange={(event) => updateForm('targetProductId', event.target.value)} /></label>
        </div>
        <div className="admin-api-console__row">
          <label>Usage limit<input type="number" value={form.usageLimit} onChange={(event) => updateForm('usageLimit', event.target.value)} /></label>
          <label>Start date<input type="datetime-local" value={form.startDate} onChange={(event) => updateForm('startDate', event.target.value)} /></label>
        </div>
        <label>End date<input type="datetime-local" value={form.endDate} onChange={(event) => updateForm('endDate', event.target.value)} /></label>
        <button type="submit" disabled={submitting}>{submitting ? 'Saving...' : form.id ? 'Update promotion' : 'Create promotion'}</button>
      </form>

      <div className="admin-resource-table">
        <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 130px 1.3fr 120px 120px 130px 220px' }}>
          <span>ID</span><span>Code</span><span>Name</span><span>Scope</span><span>Discount</span><span>Active</span><span>Actions</span>
        </div>
        {promotions.map((promotion) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 130px 1.3fr 120px 120px 130px 220px' }} key={promotion.id}>
            <span>{promotion.id}</span>
            <span>{promotion.code}</span>
            <span>{promotion.name} ({formatDateTime(promotion.endDate)})</span>
            <span>{promotion.scope}</span>
            <span>{promotion.type === 'FIXED_AMOUNT' ? formatPrice(promotion.discountValue) : promotion.discountValue}</span>
            <span>{String(promotion.active ?? promotion.isActive)}</span>
            <span className="admin-resource-table__actions">
              <button type="button" onClick={() => selectPromotion(promotion)}>Edit</button>
              <button type="button" onClick={() => handleAction(() => activatePromotion(promotion.id))}>Activate</button>
              <button type="button" className="is-danger" onClick={() => handleAction(() => deactivatePromotion(promotion.id))}>Deactivate</button>
              <button type="button" className="is-danger" onClick={() => handleAction(() => deletePromotion(promotion.id))}>Delete</button>
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}

function toDateTimeInput(value) {
  if (!value) return '';
  return String(value).slice(0, 16);
}

export default AdminPromotionPage;
