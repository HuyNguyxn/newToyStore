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
      setError(err.message || 'Upload th?t b?i.');
    } finally {
      setLoading(false);
    }
  }

  const url = result?.secureUrl || result?.url || '';

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin API</p>
          <h2>Upload Media</h2>
          <span>Upload product, avatar, review, or general media to Cloudinary through backend.</span>
        </div>
      </div>

      <form className="admin-api-console" onSubmit={handleSubmit}>
        <div className="admin-api-console__row">
          <label>
            Media type
            <select value={mediaType} onChange={(event) => setMediaType(event.target.value)}>
              <option value="image">Image</option>
              <option value="video">Video</option>
            </select>
          </label>

          <label>
            Folder
            <input value={folder} onChange={(event) => setFolder(event.target.value)} placeholder="products" />
          </label>
        </div>

        <label>
          File
          <input
            type="file"
            accept={mediaType === 'video' ? 'video/*' : 'image/*'}
            onChange={(event) => setFile(event.target.files?.[0] || null)}
          />
        </label>

        <button type="submit" disabled={loading}>{loading ? 'Uploading...' : 'Upload'}</button>
      </form>

      {error && <div className="form-alert">{error}</div>}

      {result && (
        <div className="upload-result-card">
          <h2>Upload result</h2>
          {url && mediaType === 'image' && <img src={url} alt="Uploaded media" />}
          {url && mediaType === 'video' && <video src={url} controls />}
          {url && <a href={url} target="_blank" rel="noreferrer">{url}</a>}
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </section>
  );
}

export default AdminUploadPage;
