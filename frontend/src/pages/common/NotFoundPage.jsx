import { Link } from 'react-router-dom';

function NotFoundPage() {
  return (
    <section className="not-found-page container">
      <p>404</p>
      <h1>Không tìm thấy trang</h1>
      <span>Đường dẫn này không tồn tại hoặc đã được di chuyển.</span>
      <Link to="/">Quay về trang chủ</Link>
    </section>
  );
}

export default NotFoundPage;
