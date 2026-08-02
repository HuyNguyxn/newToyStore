import { Link } from 'react-router-dom';

function NotFoundPage() {
  return (
    <section className="not-found-page container">
      <p>404</p>
      <h1>Kh?ng t?m th?y trang</h1>
      <span>Đường dẫn này không tồn tại hoặc đã được di chuyển.</span>
      <Link to="/">Quay ve trang chu</Link>
    </section>
  );
}

export default NotFoundPage;
