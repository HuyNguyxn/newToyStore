import { useEffect, useState } from 'react';
import {
  addProductImage,
  addVariantStock,
  createAdminProduct,
  deleteAdminProduct,
  getAdminProducts,
  removeProductImage,
  setProductThumbnail,
  updateAdminProduct,
  updateVariantPrice,
} from '../../services/adminProductService.js';
import { uploadImage } from '../../services/uploadService.js';
import { formatPrice } from '../../utils/formatters.js';

const emptyProductForm = {
  id: '',
  name: '',
  basePrice: '',
  categoryIds: '',
  status: 'ACTIVE',
  defaultInitialStock: '0',
  supplierId: '',
  variantsJson: '[{"attributes":{"type":"Default"},"initialStock":0,"price":0,"master":true}]',
};

function AdminProductPage() {
  const [products, setProducts] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [filters, setFilters] = useState({ status: '', minPrice: '', maxPrice: '' });
  const [form, setForm] = useState(emptyProductForm);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [imageForm, setImageForm] = useState({ imageUrl: '', thumbnail: true });
  const [variantForm, setVariantForm] = useState({ variantId: '', price: '', stockAmount: '' });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadProducts(0);
  }, []);

  function normalizePage(result) {
    return {
      content: result?.content || [],
      number: result?.number || 0,
      totalPages: result?.totalPages || 1,
      totalElements: result?.totalElements || result?.content?.length || 0,
    };
  }

  function loadProducts(page = pageInfo.number) {
    setLoading(true);
    setError('');

    getAdminProducts({ ...filters, page, size: 10, sort: 'createdAt,desc' })
      .then((result) => {
        const next = normalizePage(result);
        setProducts(next.content);
        setPageInfo({ number: next.number, totalPages: next.totalPages, totalElements: next.totalElements });
      })
      .catch((err) => setError(err.message || 'Kh?ng th? t?i s?n ph?m.'))
      .finally(() => setLoading(false));
  }

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function selectProduct(product) {
    setSelectedProduct(product);
    setForm({
      id: product.id || '',
      name: product.name || '',
      basePrice: product.basePrice ?? '',
      categoryIds: (product.categoryIds || []).join(','),
      status: product.status || 'ACTIVE',
      defaultInitialStock: '0',
      supplierId: product.supplierId || '',
      variantsJson: JSON.stringify(product.variants || [], null, 2),
    });
    setImageForm({ imageUrl: '', thumbnail: true });
    setVariantForm({ variantId: product.defaultVariantId || '', price: '', stockAmount: '' });
  }

  function buildPayload(includeVariants) {
    const payload = {
      name: form.name.trim(),
      basePrice: Number(form.basePrice || 0),
      categoryIds: form.categoryIds.split(',').map((id) => Number(id.trim())).filter(Boolean),
      status: form.status.trim() || null,
      supplierId: Number(form.supplierId),
    };

    if (includeVariants) {
      payload.defaultInitialStock = Number(form.defaultInitialStock || 0);
      payload.variants = JSON.parse(form.variantsJson || '[]');
    }

    return payload;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      if (form.id) {
        await updateAdminProduct(form.id, buildPayload(false));
        setMessage('?? c?p nh?t s?n ph?m.');
      } else {
        await createAdminProduct(buildPayload(true));
        setMessage('?? t?o s?n ph?m.');
      }
      setForm(emptyProductForm);
      setSelectedProduct(null);
      loadProducts(0);
    } catch (err) {
      setError(err.message || 'L?u s?n ph?m th?t b?i. Ki?m tra JSON variants v? c?c ID.');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUploadImage(event) {
    const file = event.target.files?.[0];
    if (!file) return;

    setSubmitting(true);
    setError('');
    try {
      const result = await uploadImage(file, 'products');
      setImageForm((current) => ({ ...current, imageUrl: result.secureUrl || result.url || '' }));
      setMessage('?? upload ?nh. B?m Add image ?? g?n v?o s?n ph?m.');
    } catch (err) {
      setError(err.message || 'Upload ?nh th?t b?i.');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAddImage() {
    if (!selectedProduct?.id || !imageForm.imageUrl.trim()) return;
    setSubmitting(true);
    setError('');
    try {
      const updated = await addProductImage(selectedProduct.id, {
        imageUrl: imageForm.imageUrl.trim(),
        thumbnail: imageForm.thumbnail,
      });
      setSelectedProduct(updated);
      setMessage('?? th?m ?nh s?n ph?m.');
      loadProducts(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Kh?ng th? th?m ?nh.');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleProductAction(action) {
    if (!selectedProduct?.id) return;
    setSubmitting(true);
    setError('');
    try {
      await action();
      setMessage('?? c?p nh?t s?n ph?m.');
      loadProducts(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Thao t?c s?n ph?m th?t b?i.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="page-message">?ang t?i s?n ph?m admin...</div>;
  }

  return (
    <section className="admin-product-page admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin CRUD</p>
          <h2>Products</h2>
          <span>Create, update, delete products, manage images, thumbnail, variant price, and stock.</span>
        </div>
        <strong>{pageInfo.totalElements} products</strong>
      </div>

      <form className="admin-filter" onSubmit={(event) => { event.preventDefault(); loadProducts(0); }}>
        {['status', 'minPrice', 'maxPrice'].map((field) => (
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
        <form className="admin-api-console" onSubmit={handleSubmit}>
          <div className="admin-panel__heading">
            <div>
              <p>{form.id ? `Edit #${form.id}` : 'Create'}</p>
              <h2>Product form</h2>
            </div>
            {form.id && <button type="button" onClick={() => { setForm(emptyProductForm); setSelectedProduct(null); }}>New</button>}
          </div>

          <div className="admin-api-console__row">
            <label>Name<input value={form.name} onChange={(event) => updateForm('name', event.target.value)} required /></label>
            <label>Base price<input type="number" value={form.basePrice} onChange={(event) => updateForm('basePrice', event.target.value)} required /></label>
          </div>
          <div className="admin-api-console__row">
            <label>Category IDs<input value={form.categoryIds} onChange={(event) => updateForm('categoryIds', event.target.value)} placeholder="1,2" required /></label>
            <label>Supplier ID<input type="number" value={form.supplierId} onChange={(event) => updateForm('supplierId', event.target.value)} required /></label>
          </div>
          <div className="admin-api-console__row">
            <label>Status<input value={form.status} onChange={(event) => updateForm('status', event.target.value)} /></label>
            <label>Initial stock<input type="number" value={form.defaultInitialStock} onChange={(event) => updateForm('defaultInitialStock', event.target.value)} disabled={Boolean(form.id)} /></label>
          </div>
          {!form.id && (
            <label>Variants JSON<textarea value={form.variantsJson} onChange={(event) => updateForm('variantsJson', event.target.value)} rows="7" /></label>
          )}
          <button type="submit" disabled={submitting}>{submitting ? 'Saving...' : form.id ? 'Update product' : 'Create product'}</button>
        </form>

        <div className="admin-api-console">
          <div className="admin-panel__heading">
            <div>
              <p>Media & Variant</p>
              <h2>{selectedProduct ? `Product #${selectedProduct.id}` : 'Select product'}</h2>
            </div>
          </div>

          <label>Upload image<input type="file" accept="image/*" onChange={handleUploadImage} disabled={!selectedProduct || submitting} /></label>
          <label>Image URL<input value={imageForm.imageUrl} onChange={(event) => setImageForm((current) => ({ ...current, imageUrl: event.target.value }))} /></label>
          <label className="inline-check"><input type="checkbox" checked={imageForm.thumbnail} onChange={(event) => setImageForm((current) => ({ ...current, thumbnail: event.target.checked }))} /> Thumbnail</label>
          <button type="button" disabled={!selectedProduct || submitting} onClick={handleAddImage}>Add image</button>

          <div className="admin-mini-gallery">
            {(selectedProduct?.images || []).map((image) => (
              <div key={image.id}>
                <img src={image.imageUrl} alt="Product" />
                <button type="button" onClick={() => handleProductAction(() => setProductThumbnail(selectedProduct.id, image.id))}>Thumbnail</button>
                <button type="button" className="is-danger" onClick={() => handleProductAction(() => removeProductImage(selectedProduct.id, image.id))}>Remove</button>
              </div>
            ))}
          </div>

          <div className="admin-api-console__row">
            <label>Variant ID<input value={variantForm.variantId} onChange={(event) => setVariantForm((current) => ({ ...current, variantId: event.target.value }))} /></label>
            <label>New price<input type="number" value={variantForm.price} onChange={(event) => setVariantForm((current) => ({ ...current, price: event.target.value }))} /></label>
          </div>
          <button type="button" disabled={!selectedProduct || !variantForm.variantId || !variantForm.price} onClick={() => handleProductAction(() => updateVariantPrice(selectedProduct.id, variantForm.variantId, variantForm.price))}>Update variant price</button>
          <label>Stock amount<input type="number" value={variantForm.stockAmount} onChange={(event) => setVariantForm((current) => ({ ...current, stockAmount: event.target.value }))} /></label>
          <button type="button" disabled={!selectedProduct || !variantForm.variantId || !variantForm.stockAmount} onClick={() => handleProductAction(() => addVariantStock(selectedProduct.id, variantForm.variantId, variantForm.stockAmount))}>Add stock</button>
        </div>
      </div>

      <div className="admin-resource-table">
        <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 1.5fr 130px 120px 120px 220px' }}>
          <span>ID</span><span>Name</span><span>Price</span><span>Status</span><span>Stock</span><span>Actions</span>
        </div>
        {products.map((product) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 1.5fr 130px 120px 120px 220px' }} key={product.id}>
            <span>{product.id}</span>
            <span>{product.name}</span>
            <span>{formatPrice(product.basePrice)}</span>
            <span>{product.status}</span>
            <span>{product.defaultVariantStockQuantity}</span>
            <span className="admin-resource-table__actions">
              <button type="button" onClick={() => selectProduct(product)}>Edit</button>
              <button type="button" className="is-danger" onClick={() => handleProductAction(() => deleteAdminProduct(product.id))}>Delete</button>
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}

export default AdminProductPage;
