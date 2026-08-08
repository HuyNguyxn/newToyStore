import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { sampleProducts } from '../../data/sampleData.js';
import { getProducts } from '../../services/productService.js';
import ProductCard from './components/ProductCard.jsx';

const heroSlides = [
  {
    image: '/toystore-assets/hero-1.png',
    eyebrow: 'ToyStore',
    title: 'Khám phá thế giới đồ chơi',
    description: 'Đồ chơi thông minh, gấu bông đáng yêu, lego sáng tạo và quà tặng an toàn cho bé.',
  },
  {
    image: '/toystore-assets/hero-2.png',
    eyebrow: 'New Collection',
    title: 'Món quà nhỏ cho niềm vui lớn',
    description: 'Chọn nhanh những sản phẩm nổi bật, phù hợp cho bé học, chơi và sáng tạo mỗi ngày.',
  },
  {
    image: '/toystore-assets/hero-3.png',
    eyebrow: 'Best Seller',
    title: 'Đồ chơi bán chạy trong tuần',
    description: 'Các mẫu đồ chơi được phụ huynh quan tâm nhiều, dễ mua và dễ thêm vào giỏ hàng.',
  },
  {
    image: '/toystore-assets/hero-4.png',
    eyebrow: 'Safe Toys',
    title: 'Ưu tiên trải nghiệm an toàn',
    description: 'Không gian mua sắm rõ ràng, thân thiện và phù hợp với cửa hàng đồ chơi hiện đại.',
  },
];

function HomePage() {
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [notice, setNotice] = useState('');
  const [activeSlide, setActiveSlide] = useState(0);
  const currentSlide = useMemo(() => heroSlides[activeSlide], [activeSlide]);

  useEffect(() => {
    let active = true;

    // Fetch products marked as featured = true first
    getProducts({ page: 0, size: 5, featured: true, sort: 'updatedAt,desc' })
      .then(async (result) => {
        let items = result.content || [];

        // If fewer than 5 featured products exist, fill up with latest active products
        if (items.length < 5) {
          try {
            const fallbackRes = await getProducts({ page: 0, size: 5, sort: 'createdAt,desc' });
            const extraItems = (fallbackRes.content || []).filter(
              (p) => !items.some((f) => f.id === p.id)
            );
            items = [...items, ...extraItems].slice(0, 5);
          } catch {
            // Keep items as is
          }
        }

        if (active) {
          setFeaturedProducts(items);
        }
      })
      .catch(() => {
        if (active) {
          setFeaturedProducts(sampleProducts.slice(0, 5));
        }
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    const timerId = window.setInterval(() => {
      setActiveSlide((current) => (current + 1) % heroSlides.length);
    }, 5000);

    return () => window.clearInterval(timerId);
  }, []);

  function goToPreviousSlide() {
    setActiveSlide((current) => (current - 1 + heroSlides.length) % heroSlides.length);
  }

  function goToNextSlide() {
    setActiveSlide((current) => (current + 1) % heroSlides.length);
  }

  return (
    <div className="home-page home-page--showcase container">
      <section className="home-page__content">
        <section className="hero-banner">
          <button className="hero-banner__arrow hero-banner__arrow--left" type="button" aria-label="Banner trước" onClick={goToPreviousSlide}>
            ‹
          </button>
          <div className="hero-banner__copy">
            <span className="hero-banner__eyebrow">{currentSlide.eyebrow}</span>
            <h1>{currentSlide.title}</h1>
            <p>{currentSlide.description}</p>
            <Link to="/products" className="hero-banner__button">Mua sắm ngay</Link>
          </div>
          <div className="hero-banner__visual" aria-hidden="true">
            <img src={currentSlide.image} alt="" />
          </div>
          <button className="hero-banner__arrow hero-banner__arrow--right" type="button" aria-label="Banner tiếp theo" onClick={goToNextSlide}>
            ›
          </button>
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
