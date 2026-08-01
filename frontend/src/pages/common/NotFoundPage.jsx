import { Link } from 'react-router-dom';

function NotFoundPage() {
  return (
    <section className="not-found-page container">
      <p>404</p>
      <h1>Khong tim thay trang</h1>
      <span>Duong dan nay khong ton tai hoac da duoc di chuyen.</span>
      <Link to="/">Quay ve trang chu</Link>
    </section>
  );
}

export default NotFoundPage;
