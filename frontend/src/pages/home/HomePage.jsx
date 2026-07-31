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
            <h1>Kham Pha The Gioi Do Choi</h1>
            <p>Kham pha do choi va mon qua thong minh giup be hoc hoi, sang tao va vui choi moi ngay.</p>
            <Link to="/products" className="hero-banner__button">Mua sam ngay</Link>
          </div>
          <div className="hero-banner__art" aria-hidden="true">
            <span>Plane</span>
            <span>Bot</span>
            <span>Car</span>
            <span>Bear</span>
          </div>
        </section>

        {notice && <div className="form-alert form-alert--soft">{notice}</div>}

        <section className="featured-section">
          <div className="section-heading">
            <h2>San Pham Noi Bat</h2>
            <Link to="/products">Xem tat ca</Link>
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
