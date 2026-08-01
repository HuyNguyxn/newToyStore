import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { sampleProducts } from '../../data/sampleData.js';
import { getProducts } from '../../services/productService.js';
import ProductCard from './components/ProductCard.jsx';

function HomePage() {
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [notice, setNotice] = useState('');

  useEffect(() => {
    let active = true;

    getProducts({ page: 0, size: 4, sort: 'createdAt,desc' })
      .then((result) => {
        if (active) {
          setFeaturedProducts(result.content || []);
        }
      })
      .catch(() => {
        if (active) {
          setFeaturedProducts(sampleProducts.slice(0, 4));
          setNotice('Backend chưa sẵn sàng, đang hiển thị dữ liệu mẫu.');
        }
      });

    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="home-page home-page--showcase container">
      <section className="home-page__content">
        <section className="hero-banner">
          <button className="hero-banner__arrow" type="button" aria-label="Previous banner">‹</button>
          <div className="hero-banner__copy">
            <span className="hero-banner__eyebrow">ToyStore</span>
            <h1>Khám phá thế giới đồ chơi</h1>
            <p>Đồ chơi thông minh, gấu bông đáng yêu, lego sáng tạo và quà tặng an toàn cho bé.</p>
            <Link to="/products" className="hero-banner__button">Mua sắm ngay</Link>
          </div>
          <div className="hero-banner__visual" aria-hidden="true">
            <img src="/toystore-assets/hero-1.png" alt="" />
          </div>
          <button className="hero-banner__arrow hero-banner__arrow--right" type="button" aria-label="Next banner">›</button>
        </section>

        <section className="store-benefits" aria-label="Lợi ích mua sắm">
          <article>
            <span>🧸</span>
            <strong>Đồ chơi an toàn</strong>
            <p>Ưu tiên sản phẩm phù hợp cho trẻ em.</p>
          </article>
          <article>
            <span>🚚</span>
            <strong>Giao hàng nội bộ</strong>
            <p>Theo dõi trạng thái giao hàng rõ ràng.</p>
          </article>
          <article>
            <span>🎁</span>
            <strong>Khuyến mãi dễ dùng</strong>
            <p>Áp mã giảm giá trực tiếp trong giỏ hàng.</p>
          </article>
        </section>

        {notice && <div className="form-alert form-alert--soft">{notice}</div>}

        <section className="featured-section">
          <div className="section-heading">
            <h2>★ Sản Phẩm Nổi Bật</h2>
            <Link to="/products">Xem tất cả →</Link>
          </div>

          <div className="product-grid">
            {featuredProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        </section>
      </section>
    </div>
  );
}

export default HomePage;
