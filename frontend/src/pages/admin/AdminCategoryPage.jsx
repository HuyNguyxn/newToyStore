import { useEffect, useState } from 'react';
import {
  createCategory,
  deleteCategory,
  getAdminCategories,
  getAdminCategoryTree,
  hideCategory,
  moveCategory,
  showCategory,
  updateCategoryInfo,
} from '../../services/adminCategoryService.js';

const emptyForm = { id: '', name: '', slug: '', description: '', iconUrl: '', displayOrder: '0', parentId: '', version: '0' };

function AdminCategoryPage() {
  const [categories, setCategories] = useState([]);
  const [tree, setTree] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [moveForm, setMoveForm] = useState({ parentId: '', displayOrder: '0', version: '0' });
  const [filters, setFilters] = useState({ keyword: '', status: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadData(); }, []);

  async function loadData() {
    try {
      const [page, treeResult] = await Promise.all([
        getAdminCategories({ ...filters, page: 0, size: 20 }),
        getAdminCategoryTree(),
      ]);
      setCategories(page.content || []);
      setTree(Array.isArray(treeResult) ? treeResult : []);
    } catch (err) { setError(err.message || 'Khong the tai category.'); }
  }

  function selectCategory(category) {
    setForm({
      id: category.id || '',
      name: category.name || '',
      slug: category.slug || '',
      description: category.description || '',
      iconUrl: category.iconUrl || '',
      displayOrder: category.displayOrder ?? '0',
      parentId: category.parentId || '',
      version: category.version ?? '0',
    });
    setMoveForm({ parentId: category.parentId || '', displayOrder: category.displayOrder ?? '0', version: category.version ?? '0' });
  }

  async function saveCategory(event) {
    event.preventDefault(); setMessage(''); setError('');
    try {
      const payload = {
        name: form.name.trim(), slug: form.slug.trim(), description: form.description || null,
        iconUrl: form.iconUrl || null, displayOrder: Number(form.displayOrder || 0),
        parentId: form.parentId === '' ? null : Number(form.parentId),
        ...(form.id ? { version: Number(form.version || 0) } : {}),
      };
      if (form.id) await updateCategoryInfo(form.id, payload); else await createCategory(payload);
      setMessage(form.id ? 'Da cap nhat category.' : 'Da tao category.');
      setForm(emptyForm); await loadData();
    } catch (err) { setError(err.message || 'Luu category that bai.'); }
  }

  async function doAction(action, success) {
    setMessage(''); setError('');
    try { await action(); setMessage(success); await loadData(); } catch (err) { setError(err.message || 'Thao tac category that bai.'); }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero"><div><p>Admin CRUD</p><h2>Categories</h2><span>Create, update, move, show, hide, and delete categories.</span></div></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadData(); }}>
        <label>keyword<input value={filters.keyword} onChange={(e) => setFilters((c) => ({ ...c, keyword: e.target.value }))} /></label>
        <label>status<input value={filters.status} onChange={(e) => setFilters((c) => ({ ...c, status: e.target.value }))} /></label>
        <button type="submit">Filter</button>
      </form>
      <div className="admin-crud-grid">
        <form className="admin-api-console" onSubmit={saveCategory}>
          <div className="admin-panel__heading"><div><p>{form.id ? `Edit #${form.id}` : 'Create'}</p><h2>Category form</h2></div><button type="button" onClick={() => setForm(emptyForm)}>New</button></div>
          <div className="admin-api-console__row"><label>Name<input value={form.name} onChange={(e) => setForm((c) => ({ ...c, name: e.target.value }))} required /></label><label>Slug<input value={form.slug} onChange={(e) => setForm((c) => ({ ...c, slug: e.target.value }))} required /></label></div>
          <label>Description<input value={form.description} onChange={(e) => setForm((c) => ({ ...c, description: e.target.value }))} /></label>
          <div className="admin-api-console__row"><label>Icon URL<input value={form.iconUrl} onChange={(e) => setForm((c) => ({ ...c, iconUrl: e.target.value }))} /></label><label>Parent ID<input value={form.parentId} onChange={(e) => setForm((c) => ({ ...c, parentId: e.target.value }))} /></label></div>
          <div className="admin-api-console__row"><label>Order<input value={form.displayOrder} onChange={(e) => setForm((c) => ({ ...c, displayOrder: e.target.value }))} /></label><label>Version<input value={form.version} onChange={(e) => setForm((c) => ({ ...c, version: e.target.value }))} /></label></div>
          <button type="submit">{form.id ? 'Update category' : 'Create category'}</button>
        </form>
        <form className="admin-api-console" onSubmit={(e) => { e.preventDefault(); doAction(() => moveCategory(form.id, { parentId: moveForm.parentId === '' ? null : Number(moveForm.parentId), displayOrder: Number(moveForm.displayOrder), version: Number(moveForm.version) }), 'Da move category.'); }}>
          <div className="admin-panel__heading"><div><p>Tree</p><h2>Move selected category</h2></div></div>
          <div className="admin-api-console__row"><label>New parent ID<input value={moveForm.parentId} onChange={(e) => setMoveForm((c) => ({ ...c, parentId: e.target.value }))} /></label><label>Order<input value={moveForm.displayOrder} onChange={(e) => setMoveForm((c) => ({ ...c, displayOrder: e.target.value }))} /></label></div>
          <label>Version<input value={moveForm.version} onChange={(e) => setMoveForm((c) => ({ ...c, version: e.target.value }))} /></label>
          <button type="submit" disabled={!form.id}>Move category</button>
          <CategoryTree nodes={tree} onSelect={selectCategory} />
        </form>
      </div>
      <SimpleTable rows={categories} onSelect={selectCategory} onShow={(id) => doAction(() => showCategory(id), 'Da show category.')} onHide={(id) => doAction(() => hideCategory(id), 'Da hide category.')} onDelete={(id) => doAction(() => deleteCategory(id), 'Da xoa category.')} />
    </section>
  );
}

function SimpleTable({ rows, onSelect, onShow, onHide, onDelete }) {
  return <div className="admin-resource-table"><div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 1fr 1fr 120px 240px' }}><span>ID</span><span>Name</span><span>Slug</span><span>Status</span><span>Actions</span></div>{rows.map((row) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 1fr 1fr 120px 240px' }} key={row.id}><span>{row.id}</span><span>{row.name}</span><span>{row.slug}</span><span>{row.status}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => onSelect(row)}>Edit</button><button type="button" onClick={() => onShow(row.id)}>Show</button><button type="button" onClick={() => onHide(row.id)}>Hide</button><button type="button" className="is-danger" onClick={() => onDelete(row.id)}>Delete</button></span></div>)}</div>;
}

function CategoryTree({ nodes, onSelect }) {
  if (!nodes?.length) {
    return <div className="empty-state">Chua co category tree.</div>;
  }

  return (
    <div className="admin-tree">
      {nodes.map((node) => <CategoryNode key={node.id} node={node} onSelect={onSelect} />)}
    </div>
  );
}

function CategoryNode({ node, onSelect }) {
  const children = node.children || node.childCategories || [];

  return (
    <div className="admin-tree__branch">
      <button type="button" className="admin-tree__node" onClick={() => onSelect(node)}>
        <strong>{node.name}</strong>
        <span>#{node.id} · {node.status || 'UNKNOWN'} · order {node.displayOrder ?? 0}</span>
      </button>
      {children.length > 0 && (
        <div className="admin-tree__children">
          {children.map((child) => <CategoryNode key={child.id} node={child} onSelect={onSelect} />)}
        </div>
      )}
    </div>
  );
}

export default AdminCategoryPage;
