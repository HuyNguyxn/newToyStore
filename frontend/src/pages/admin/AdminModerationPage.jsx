import { useEffect, useState } from 'react';
import {
  createBlacklistedWord,
  deleteBlacklistedWord,
  getBlacklistedWords,
  hardDeleteBlacklistedWord,
  restoreBlacklistedWord,
  updateBlacklistedWord,
} from '../../services/adminModerationService.js';
import { formatDateTime } from '../../utils/formatters.js';

const emptyForm = { id: '', word: '', category: 'PROFANITY' };
const categories = ['PROFANITY', 'SPAM', 'COMPETITOR', 'OTHER'];

function getCategoryLabel(category) {
  const cat = String(category || '').toUpperCase();
  if (cat === 'PROFANITY') return 'Từ tục tĩu';
  if (cat === 'SPAM') return 'Spam quảng cáo';
  if (cat === 'COMPETITOR') return 'Đối thủ cạnh tranh';
  return 'Khác';
}

function AdminModerationPage() {
  const [words, setWords] = useState([]);
  const [totalWords, setTotalWords] = useState(0);
  const [filters, setFilters] = useState({ keyword: '', category: '' });
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadWords();
  }, []);

  async function loadWords() {
    setLoading(true);
    setError('');
    try {
      const result = await getBlacklistedWords({
        keyword: filters.keyword || undefined,
        category: filters.category || undefined,
        page: 0,
        size: 50,
        sort: 'createdAt,desc',
      });
      setWords(result.content || result || []);
      setTotalWords(Number(result?.totalElements ?? (Array.isArray(result) ? result.length : 0)));
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách từ khóa cấm.');
      setWords([]);
      setTotalWords(0);
    } finally {
      setLoading(false);
    }
  }

  function selectWord(word) {
    setForm({
      id: word.id || '',
      word: word.word || word.value || '',
      category: word.category?.code || word.category || word.type || 'PROFANITY',
    });
  }

  async function saveWord(event) {
    event.preventDefault();
    if (!form.word.trim()) return;
    setError('');
    setMessage('');
    try {
      const payload = { word: form.word.trim(), category: form.category };
      if (form.id) {
        await updateBlacklistedWord(form.id, payload);
      } else {
        await createBlacklistedWord(payload);
      }
      setMessage(form.id ? 'Cập nhật từ khóa cấm thành công.' : 'Thêm từ khóa cấm mới thành công.');
      setForm(emptyForm);
      await loadWords();
    } catch (err) {
      setError(err.message || 'Lưu từ khóa cấm thất bại.');
    }
  }

  async function doAction(action, successMsg) {
    setError('');
    setMessage('');
    try {
      await action();
      setMessage(successMsg);
      await loadWords();
    } catch (err) {
      setError(err.message || 'Thao tác từ khóa thất bại.');
    }
  }

  const handleClearFilters = () => {
    setFilters({ keyword: '', category: '' });
    setTimeout(() => {
      loadWords();
    }, 50);
  };

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Kiểm duyệt từ khóa (Blacklist Words)
        </h1>
        <div style={{ background: '#fff7ed', color: '#ea580c', border: '1px solid #ffedd5', padding: '6px 16px', borderRadius: '20px', fontSize: '13px', fontWeight: '700' }}>
          Từ khóa: {totalWords}
        </div>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* FILTER BAR */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          loadWords();
        }}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '16px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '200px' }}>
          <input
            type="text"
            placeholder="Tìm theo từ khóa..."
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '200px' }}>
          <select
            value={filters.category}
            onChange={(e) => setFilters({ ...filters, category: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả danh mục từ cấm</option>
            {categories.map((cat) => (
              <option key={cat} value={cat}>
                {getCategoryLabel(cat)} ({cat})
              </option>
            ))}
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

      {/* GRID LAYOUT */}
      <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: '20px', alignItems: 'start' }}>
        
        {/* Left Console Card: Create/Edit Word */}
        <form
          onSubmit={saveWord}
          style={{ background: '#ffffff', padding: '20px', borderRadius: '12px', border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', gap: '14px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: 0 }}>
            <h3 style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a' }}>
              {form.id ? `Chỉnh sửa #${form.id}` : 'Thêm từ khóa mới'}
            </h3>
            {form.id && (
              <button
                type="button"
                onClick={() => setForm(emptyForm)}
                style={{ border: 'none', background: '#f1f5f9', color: '#ea580c', borderRadius: '6px', padding: '4px 10px', fontSize: '12px', fontWeight: '700', cursor: 'pointer' }}
              >
                Hủy sửa
              </button>
            )}
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Từ khóa cấm *</label>
            <input
              type="text"
              placeholder="Nhập từ cấm..."
              value={form.word}
              onChange={(e) => setForm({ ...form, word: e.target.value })}
              required
              style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Danh mục phân loại</label>
            <select
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
              style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
            >
              {categories.map((item) => (
                <option key={item} value={item}>
                  {getCategoryLabel(item)}
                </option>
              ))}
            </select>
          </div>

          <button
            type="submit"
            style={{ width: '100%', padding: '10.5px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '800', cursor: 'pointer', marginTop: '4px' }}
          >
            {form.id ? 'Cập nhật từ khóa' : 'Lưu từ khóa cấm'}
          </button>
        </form>

        {/* Right Table Container */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'visible' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                <th style={{ padding: '14px 16px', width: '60px' }}>ID</th>
                <th style={{ padding: '14px 16px' }}>Từ khóa cấm</th>
                <th style={{ padding: '14px 16px', width: '160px' }}>Danh mục</th>
                <th style={{ padding: '14px 16px', width: '150px' }}>Ngày tạo</th>
                <th style={{ padding: '14px 16px', width: '220px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading && words.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
                    Đang tải danh sách từ khóa cấm...
                  </td>
                </tr>
              ) : words.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                    Không tìm thấy từ khóa cấm nào.
                  </td>
                </tr>
              ) : (
                words.map((word, idx) => {
                  const isDeleted = word.deleted || word.status === 'DELETED';
                  return (
                    <tr key={word.id} style={{ borderBottom: '1px solid #f1f5f9', background: idx % 2 === 0 ? '#ffffff' : '#fafafa' }}>
                      <td style={{ padding: '14px 16px', fontWeight: '600', color: '#334155' }}>
                        #{word.id}
                      </td>
                      <td style={{ padding: '14px 16px', fontWeight: '700', color: isDeleted ? '#cbd5e1' : '#334155', textDecoration: isDeleted ? 'line-through' : 'none' }}>
                        {word.word || word.value}
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <span style={{ background: '#f1f5f9', color: '#475569', padding: '3px 8px', borderRadius: '6px', fontSize: '11.5px', fontWeight: '700' }}>
                          {getCategoryLabel(word.category?.code || word.category || word.type)}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px', color: '#64748b' }}>
                        {formatDateTime(word.createdAt)}
                      </td>
                      <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                        <div style={{ display: 'inline-flex', gap: '4px', justifyContent: 'center' }}>
                          <button
                            type="button"
                            onClick={() => selectWord(word)}
                            style={{ padding: '5px 10px', background: '#ffffff', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: '700' }}
                          >
                            Sửa
                          </button>
                          {isDeleted ? (
                            <button
                              type="button"
                              onClick={() => doAction(() => restoreBlacklistedWord(word.id), 'Khôi phục từ khóa thành công.')}
                              style={{ padding: '5px 10px', background: '#eff6ff', color: '#2563eb', border: '1px solid #bfdbfe', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: '700' }}
                            >
                              Khôi phục
                            </button>
                          ) : (
                            <button
                              type="button"
                              onClick={() => doAction(() => deleteBlacklistedWord(word.id), 'Đã xóa tạm thời từ khóa.')}
                              style={{ padding: '5px 10px', background: '#ffffff', color: '#dc2626', border: '1px solid #fecaca', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: '700' }}
                            >
                              Xóa tạm
                            </button>
                          )}
                          <button
                            type="button"
                            onClick={() => doAction(() => hardDeleteBlacklistedWord(word.id), 'Đã xóa vĩnh viễn từ khóa cấm.')}
                            style={{ padding: '5px 10px', background: '#ffffff', color: '#991b1b', border: '1px solid #fca5a5', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: '700' }}
                          >
                            Xóa hẳn
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminModerationPage;
