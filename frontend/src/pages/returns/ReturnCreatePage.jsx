import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { createCustomerReturn } from '../../services/customerReturnService.js';
import { uploadImage } from '../../services/uploadService.js';

const emptyReturnItem = {
  orderItemId: '',
  productId: '',
  variantId: '',
  quantity: '1',
  reasonCode: 'DAMAGED',
  expectedRefundAmount: '0',
};

function ReturnCreatePage() {
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState({
    orderId: searchParams.get('orderId') || '',
    reasonNote: '',
    proofImageUrls: '',
  });
  const [items, setItems] = useState([{
    ...emptyReturnItem,
    orderItemId: searchParams.get('orderItemId') || '',
    productId: searchParams.get('productId') || '',
    variantId: searchParams.get('variantId') || '',
  }]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  async function uploadProof(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      const result = await uploadImage(file, 'returns');
      const url = result.secureUrl || result.url;
      setForm((current) => ({ ...current, proofImageUrls: [current.proofImageUrls, url].filter(Boolean).join(',') }));
    } catch (err) {
      setError(err.message || 'Upload anh minh chung that bai.');
    } finally {
      setUploading(false);
    }
  }

  function updateItem(index, field, value) {
    setItems((current) => current.map((item, itemIndex) => itemIndex === index ? { ...item, [field]: value } : item));
  }

  function addItem() {
    setItems((current) => [...current, { ...emptyReturnItem }]);
  }

  function removeItem(index) {
    setItems((current) => current.length === 1 ? current : current.filter((_, itemIndex) => itemIndex !== index));
  }

  function buildItemsPayload() {
    return items.map((item) => ({
      orderItemId: Number(item.orderItemId),
      productId: Number(item.productId),
      variantId: item.variantId === '' ? null : Number(item.variantId),
      quantity: Number(item.quantity),
      reasonCode: item.reasonCode,
      expectedRefundAmount: Number(item.expectedRefundAmount),
    }));
  }

  async function submitReturn(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await createCustomerReturn({
        orderId: Number(form.orderId),
        reasonNote: form.reasonNote || null,
        proofImageUrls: form.proofImageUrls.split(',').map((url) => url.trim()).filter(Boolean),
        items: buildItemsPayload(),
      });
      setMessage('Da gui yeu cau tra hang.');
    } catch (err) {
      setError(err.message || 'Tao yeu cau tra hang that bai.');
    }
  }

  return (
    <section className="container profile-page">
      <form className="admin-api-console" onSubmit={submitReturn}>
        <div className="admin-panel__heading">
          <div>
            <p>Customer Return</p>
            <h2>Tao yeu cau tra hang</h2>
          </div>
        </div>
        {error && <div className="form-alert">{error}</div>}
        {message && <div className="form-alert form-alert--success">{message}</div>}
        <label>Order ID<input value={form.orderId} onChange={(e) => setForm((current) => ({ ...current, orderId: e.target.value }))} required /></label>
        <label>Reason note<input value={form.reasonNote} onChange={(e) => setForm((current) => ({ ...current, reasonNote: e.target.value }))} /></label>
        <label>Upload proof image<input type="file" accept="image/*" onChange={uploadProof} disabled={uploading} /></label>
        <label>Proof image URLs<input value={form.proofImageUrls} onChange={(e) => setForm((current) => ({ ...current, proofImageUrls: e.target.value }))} /></label>

        <div className="admin-line-items">
          <div className="admin-line-items__title">
            <strong>Return items</strong>
            <button type="button" onClick={addItem}>+ Add item</button>
          </div>
          {items.map((item, index) => (
            <div className="admin-line-item" key={`return-item-${index}`}>
              <label>Order item ID<input value={item.orderItemId} onChange={(e) => updateItem(index, 'orderItemId', e.target.value)} required /></label>
              <label>Product ID<input value={item.productId} onChange={(e) => updateItem(index, 'productId', e.target.value)} required /></label>
              <label>Variant ID<input value={item.variantId} onChange={(e) => updateItem(index, 'variantId', e.target.value)} /></label>
              <label>Qty<input type="number" min="1" value={item.quantity} onChange={(e) => updateItem(index, 'quantity', e.target.value)} required /></label>
              <label>Reason
                <select value={item.reasonCode} onChange={(e) => updateItem(index, 'reasonCode', e.target.value)}>
                  <option value="DAMAGED">DAMAGED</option>
                  <option value="WRONG_ITEM">WRONG_ITEM</option>
                  <option value="NOT_AS_DESCRIBED">NOT_AS_DESCRIBED</option>
                  <option value="CUSTOMER_CHANGED_MIND">CUSTOMER_CHANGED_MIND</option>
                </select>
              </label>
              <label>Expected refund<input type="number" min="0" value={item.expectedRefundAmount} onChange={(e) => updateItem(index, 'expectedRefundAmount', e.target.value)} /></label>
              <button type="button" className="is-danger" onClick={() => removeItem(index)} disabled={items.length === 1}>Remove</button>
            </div>
          ))}
        </div>

        <button type="submit">Gui yeu cau tra hang</button>
      </form>
    </section>
  );
}

export default ReturnCreatePage;
