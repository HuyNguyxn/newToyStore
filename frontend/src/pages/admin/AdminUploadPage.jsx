import { useState } from 'react';
import { uploadImage, uploadVideo } from '../../services/uploadService.js';

function AdminUploadPage() {
  const [mediaType, setMediaType] = useState('image');
  const [folder, setFolder] = useState('products');
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    if (!file) {
      setError('Vui lòng chọn file để upload.');
      return;
    }

    setLoading(true);
    setError('');
    setResult(null);

    try {
      const uploaded = mediaType === 'video'
        ? await uploadVideo(file, folder)
        : await uploadImage(file, folder);
      setResult(uploaded);
    } catch (err) {
      setError(err.message || 'Tải tệp tin lên hệ thống thất bại.');
    } finally {
      setLoading(false);
    }
  }

  const url = result?.secureUrl || result?.url || '';

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Tải lên đa phương tiện (Upload Media)
        </h1>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '24px', alignItems: 'start' }}>
        
        {/* Upload Form Card */}
        <form
          onSubmit={handleSubmit}
          style={{ background: '#ffffff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', gap: '14px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}
        >
          <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: 0 }}>
            Tải tệp tin mới lên máy chủ Cloudinary
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Loại tệp tin</label>
              <select
                value={mediaType}
                onChange={(event) => setMediaType(event.target.value)}
                style={{ width: '100%', padding: '9px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none', background: '#fff' }}
              >
                <option value="image">Hình ảnh (Image)</option>
                <option value="video">Video</option>
              </select>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Thư mục lưu trữ</label>
              <input
                type="text"
                placeholder="products..."
                value={folder}
                onChange={(event) => setFolder(event.target.value)}
                style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Chọn tệp tin nguồn</label>
            <input
              type="file"
              accept={mediaType === 'video' ? 'video/*' : 'image/*'}
              onChange={(event) => setFile(event.target.files?.[0] || null)}
              style={{ width: '100%', padding: '6px 8px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            style={{ width: '100%', padding: '11px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13.5px', fontWeight: '800', cursor: 'pointer', marginTop: '4px' }}
          >
            {loading ? 'Đang tải lên...' : 'Bắt đầu tải lên'}
          </button>
        </form>

        {/* Upload Result Preview Card */}
        {result && (
          <div style={{ background: '#ffffff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 16px rgba(0,0,0,0.02)', display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: 0 }}>
              Kết quả tải lên thành công
            </h3>

            {url && mediaType === 'image' && (
              <img
                src={url}
                alt="Uploaded media preview"
                style={{ width: '100%', maxHeight: '200px', objectFit: 'contain', borderRadius: '8px', border: '1px solid #e2e8f0', background: '#f8fafc' }}
              />
            )}
            
            {url && mediaType === 'video' && (
              <video
                src={url}
                controls
                style={{ width: '100%', maxHeight: '200px', borderRadius: '8px', border: '1px solid #e2e8f0', background: '#f8fafc' }}
              />
            )}

            <div style={{ fontSize: '13px' }}>
              <span style={{ color: '#64748b', display: 'block', marginBottom: '4px' }}>Đường dẫn bảo mật (Secure URL):</span>
              <a
                href={url}
                target="_blank"
                rel="noreferrer"
                style={{ color: '#2563eb', fontWeight: '700', wordBreak: 'break-all', textDecoration: 'none' }}
              >
                {url}
              </a>
            </div>

            <div style={{ borderTop: '1px solid #f1f5f9', paddingTop: '10px' }}>
              <span style={{ color: '#64748b', display: 'block', fontSize: '12.5px', marginBottom: '6px' }}>JSON dữ liệu phản hồi:</span>
              <pre style={{ margin: 0, padding: '12px', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '11px', overflowX: 'auto', fontFamily: 'monospace' }}>
                {JSON.stringify(result, null, 2)}
              </pre>
            </div>
          </div>
        )}

      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminUploadPage;
