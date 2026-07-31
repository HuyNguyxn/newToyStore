import CategoryMenu from './components/CategoryMenu.jsx';
import ProductCard from './components/ProductCard.jsx';

const featuredProducts = [
  {
    id: 1,
    name: 'Kham pha thi choi',
    price: 350000,
    oldPrice: 450000,
    rating: 4.8,
    reviewCount: 12,
    status: 'Dang ban',
    quickAddAvailable: true,
  },
  {
    id: 2,
    name: 'Robot lap rap thong minh',
    price: 420000,
    oldPrice: 520000,
    rating: 4.7,
    reviewCount: 18,
    status: 'Dang ban',
    quickAddAvailable: false,
  },
  {
    id: 3,
    name: 'Gau bong mem mini',
    price: 180000,
    oldPrice: 220000,
    rating: 4.9,
    reviewCount: 31,
    status: 'Dang ban',
    quickAddAvailable: true,
  },
  {
    id: 4,
    name: 'Xe dieu khien dia hinh',
    price: 550000,
    oldPrice: 650000,
    rating: 4.6,
    reviewCount: 9,
    status: 'Dang ban',
    quickAddAvailable: false,
  },
];

function HomePage() {
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
            <a href="/products" className="hero-banner__button">Mua sam ngay</a>
          </div>
          <div className="hero-banner__art" aria-hidden="true">
            <span>✈</span>
            <span>▣</span>
            <span>🚗</span>
            <span>🧸</span>
          </div>
        </section>

        <section className="featured-section">
          <div className="section-heading">
            <h2>San Pham Noi Bat</h2>
            <a href="/products">Xem tat ca</a>
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
