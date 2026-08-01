import { useEffect, useState } from 'react';
import { createBlacklistedWord, deleteBlacklistedWord, getBlacklistedWords, hardDeleteBlacklistedWord, restoreBlacklistedWord, updateBlacklistedWord } from '../../services/adminModerationService.js';
import { formatDateTime } from '../../utils/formatters.js';

const emptyForm = { id: '', word: '', category: 'PROFANITY' };
const categories = ['PROFANITY', 'SPAM', 'COMPETITOR', 'OTHER'];

function AdminModerationPage() {
  const [words, setWords] = useState([]);
  const [filters, setFilters] = useState({ keyword: '', category: '' });
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadWords(); }, []);

  async function loadWords() {
    try {
      const result = await getBlacklistedWords({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' });
      setWords(result.content || []);
    } catch (err) {
      setError(err.message || 'Khong the tai moderation blacklist.');
    }
  }

  function selectWord(word) {
    setForm({ id: word.id || '', word: word.word || word.value || '', category: word.category?.code || word.category || word.type || 'PROFANITY' });
  }

  async function saveWord(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      const payload = { word: form.word.trim(), category: form.category };
      if (form.id) await updateBlacklistedWord(form.id, payload); else await createBlacklistedWord(payload);
      setMessage(form.id ? 'Da cap nhat blacklist word.' : 'Da them blacklist word.');
      setForm(emptyForm);
      await loadWords();
    } catch (err) {
      setError(err.message || 'Luu blacklist word that bai.');
    }
  }

  async function doAction(action, success) {
    setError('');
    setMessage('');
    try {
      await action();
      setMessage(success);
      await loadWords();
    } catch (err) {
      setError(err.message || 'Thao tac moderation that bai.');
    }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero"><div><p>Admin Safety</p><h2>Moderation Blacklist</h2><span>Create and maintain blocked words used by review/content moderation.</span></div></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadWords(); }}>{Object.keys(filters).map((field) => <label key={field}>{field}<input value={filters[field]} onChange={(e) => setFilters((current) => ({ ...current, [field]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>

      <div className="admin-crud-grid">
        <form className="admin-api-console" onSubmit={saveWord}>
          <div className="admin-panel__heading"><div><p>{form.id ? `Edit #${form.id}` : 'Create'}</p><h2>Blacklist word</h2></div><button type="button" onClick={() => setForm(emptyForm)}>New</button></div>
          <label>Word<input value={form.word} onChange={(e) => setForm((current) => ({ ...current, word: e.target.value }))} required /></label>
          <label>Category<select value={form.category} onChange={(e) => setForm((current) => ({ ...current, category: e.target.value }))}>{categories.map((item) => <option key={item} value={item}>{item}</option>)}</select></label>
          <button type="submit">{form.id ? 'Update word' : 'Create word'}</button>
        </form>

        <div className="admin-resource-table">
          <div className="admin-resource-table__head" style={{ gridTemplateColumns: '70px 1fr 130px 160px 240px' }}><span>ID</span><span>Word</span><span>Category</span><span>Created</span><span>Actions</span></div>
          {words.map((word) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '70px 1fr 130px 160px 240px' }} key={word.id}><span>{word.id}</span><span>{word.word || word.value}</span><span>{word.category?.code || word.category || word.type}</span><span>{formatDateTime(word.createdAt)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectWord(word)}>Edit</button><button type="button" onClick={() => doAction(() => restoreBlacklistedWord(word.id), 'Da restore word.')}>Restore</button><button type="button" className="is-danger" onClick={() => doAction(() => deleteBlacklistedWord(word.id), 'Da xoa mem word.')}>Delete</button><button type="button" className="is-danger" onClick={() => doAction(() => hardDeleteBlacklistedWord(word.id), 'Da hard delete word.')}>Hard</button></span></div>)}
        </div>
      </div>
    </section>
  );
}

export default AdminModerationPage;
