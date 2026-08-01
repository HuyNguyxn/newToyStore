import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { sampleProducts } from '../../data/sampleData.js';
import { getProducts } from '../../services/productService.js';
import CategoryMenu from './components/CategoryMenu.jsx';
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
          setNotice('Backend chua san sang, dang hien thi du lieu mau.');
        }
      });

    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="home-page container">
      <aside className="home-page__sidebar">
        <CategoryMenu />
      </aside>

      <section className="home-page__content">
        <section className="hero-banner">
          <div className="hero-banner__copy">
            <span className="hero-banner__eyebrow">New Toy Store</span>
            <h1>Kham Pha The Gioi Do Choi</h1>
            <p>Kham pha do choi thong minh, gau bong dang yeu, lego sang tao va qua tang an toan cho be.</p>
            <Link to="/products" className="hero-banner__button">Mua sam ngay</Link>
          </div>
          <div className="hero-banner__visual" aria-hidden="true">
            <img src="/toystore-assets/hero-1.png" alt="" />
          </div>
        </section>

        <section className="store-benefits" aria-label="Loi ich mua sam">
          <article>
            <span>🧸</span>
            <strong>Do choi an toan</strong>
            <p>Uu tien san pham phu hop cho tre em.</p>
          </article>
          <article>
            <span>🚚</span>
            <strong>Giao hang noi bo</strong>
            <p>Theo doi trang thai giao hang ro rang.</p>
          </article>
          <article>
            <span>🎁</span>
            <strong>Khuyen mai de dung</strong>
            <p>Ap ma giam gia truc tiep trong gio hang.</p>
          </article>
        </section>

        {notice && <div className="form-alert form-alert--soft">{notice}</div>}

        <section className="featured-section">
          <div className="section-heading">
            <h2>⭐ San Pham Noi Bat</h2>
            <Link to="/products">Xem tat ca →</Link>
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
