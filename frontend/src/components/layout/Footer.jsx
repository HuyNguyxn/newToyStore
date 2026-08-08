import { Link } from 'react-router-dom';

function Footer() {
  return (
    <footer
      style={{
        background: '#ffffff',
        borderTop: '1px solid #f1f5f9',
        padding: '48px 0 24px 0',
        color: '#475569',
        fontFamily: 'system-ui, -apple-system, sans-serif',
      }}
    >
      <div
        style={{
          maxWidth: '1200px',
          margin: '0 auto',
          padding: '0 24px',
        }}
      >
        {/* 3-COLUMN GRID */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '40px',
            marginBottom: '40px',
          }}
        >
          {/* COLUMN 1: THÔNG TIN LIÊN HỆ */}
          <div>
            <h3
              style={{
                color: '#ea580c',
                fontSize: '15px',
                fontWeight: '900',
                textTransform: 'uppercase',
                letterSpacing: '0.5px',
                marginBottom: '20px',
              }}
            >
              THÔNG TIN LIÊN HỆ
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '14px', lineHeight: '1.6', color: '#475569' }}>
              <p style={{ margin: 0 }}>
                <strong style={{ color: '#334155' }}>Địa chỉ:</strong> Shophouse, 1-1.1 CT3 Gelexia Riverside, P.Hoàng Mai, TP. Hà Nội
              </p>
              <p style={{ margin: 0 }}>
                <strong style={{ color: '#334155' }}>Hotline:</strong>{' '}
                <a href="tel:0777174355" style={{ color: '#ea580c', textDecoration: 'none', fontWeight: '700' }}>
                  0777174355
                </a>
              </p>
              <p style={{ margin: 0 }}>
                <strong style={{ color: '#334155' }}>Email:</strong>{' '}
                <a href="mailto:support@toystore.com" style={{ color: '#2563eb', textDecoration: 'none', fontWeight: '600' }}>
                  support@toystore.com
                </a>
              </p>
              <p style={{ margin: 0, color: '#64748b', fontWeight: '600' }}>
                Thứ 2 – CN: 8:00 – 18:00
              </p>
            </div>
          </div>

          {/* COLUMN 2: ĐIỀU KHOẢN & CHÍNH SÁCH */}
          <div>
            <h3
              style={{
                color: '#ea580c',
                fontSize: '15px',
                fontWeight: '900',
                textTransform: 'uppercase',
                letterSpacing: '0.5px',
                marginBottom: '20px',
              }}
            >
              ĐIỀU KHOẢN & CHÍNH SÁCH
            </h3>

            <ul
              style={{
                listStyle: 'none',
                padding: 0,
                margin: 0,
                display: 'flex',
                flexDirection: 'column',
                gap: '14px',
                fontSize: '14px',
              }}
            >
              {[
                { label: 'Chính sách đổi trả', to: '/policy/returns' },
                { label: 'Chính sách bảo mật', to: '/policy/privacy' },
              ].map((item, idx) => (
                <li key={idx} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ color: '#ea580c', fontWeight: 'bold' }}>•</span>
                  <Link
                    to={item.to}
                    style={{
                      color: '#334155',
                      textDecoration: 'none',
                      fontWeight: '700',
                      transition: 'color 0.2s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.color = '#ea580c')}
                    onMouseLeave={(e) => (e.currentTarget.style.color = '#334155')}
                  >
                    {item.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* COLUMN 3: THEO DÕI CHÚNG TÔI */}
          <div>
            <h3
              style={{
                color: '#ea580c',
                fontSize: '15px',
                fontWeight: '900',
                textTransform: 'uppercase',
                letterSpacing: '0.5px',
                marginBottom: '20px',
              }}
            >
              THEO DÕI CHÚNG TÔI
            </h3>

            <div style={{ display: 'flex', gap: '14px', alignItems: 'center' }}>
              {/* FACEBOOK BUTTON */}
              <a
                href="https://facebook.com"
                target="_blank"
                rel="noreferrer"
                title="Facebook"
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '50%',
                  border: '1.5px solid #cbd5e1',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#64748b',
                  fontSize: '18px',
                  fontWeight: '700',
                  textDecoration: 'none',
                  transition: 'all 0.2s ease',
                  background: '#ffffff',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.borderColor = '#1877f2';
                  e.currentTarget.style.color = '#1877f2';
                  e.currentTarget.style.transform = 'translateY(-2px)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = '#cbd5e1';
                  e.currentTarget.style.color = '#64748b';
                  e.currentTarget.style.transform = 'translateY(0)';
                }}
              >
                f
              </a>

              {/* TIKTOK BUTTON */}
              <a
                href="https://tiktok.com"
                target="_blank"
                rel="noreferrer"
                title="TikTok"
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '50%',
                  border: '1.5px solid #cbd5e1',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#64748b',
                  fontSize: '16px',
                  fontWeight: '700',
                  textDecoration: 'none',
                  transition: 'all 0.2s ease',
                  background: '#ffffff',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.borderColor = '#000000';
                  e.currentTarget.style.color = '#000000';
                  e.currentTarget.style.transform = 'translateY(-2px)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = '#cbd5e1';
                  e.currentTarget.style.color = '#64748b';
                  e.currentTarget.style.transform = 'translateY(0)';
                }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19.589 6.686a4.793 4.793 0 0 1-3.77-4.245V2h-3.445v13.672a2.896 2.896 0 0 1-2.9 2.891 2.892 2.892 0 0 1-2.895-2.891 2.893 2.893 0 0 1 2.895-2.892c.373 0 .727.07 1.053.197V9.431a6.31 6.31 0 0 0-1.053-.088 6.34 6.34 0 0 0-6.335 6.336 6.34 6.34 0 0 0 6.335 6.336 6.335 6.335 0 0 0 6.336-6.336V8.775a8.214 8.214 0 0 0 4.779 1.516V6.846a4.84 4.84 0 0 1-1.000-.16z" />
                </svg>
              </a>
            </div>
          </div>
        </div>

        {/* BOTTOM COPYRIGHT BAR */}
        <div
          style={{
            borderTop: '1px solid #f1f5f9',
            paddingTop: '20px',
            textAlign: 'center',
            fontSize: '13px',
            color: '#94a3b8',
          }}
        >
          © 2026 ToyStore. Tất cả các quyền được bảo lưu.
        </div>
      </div>
    </footer>
  );
}

export default Footer;
