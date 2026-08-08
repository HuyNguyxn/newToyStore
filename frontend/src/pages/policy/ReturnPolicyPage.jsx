import { Link } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';

function ReturnPolicyPage() {
  return (
    <div className="container" style={{ paddingTop: '24px', paddingBottom: '48px', maxWidth: '900px' }}>
      <BackLink fallback="/" label="Trở về Trang chủ" />

      <div style={{ background: '#ffffff', borderRadius: '20px', padding: '36px 40px', boxShadow: '0 4px 20px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9', marginTop: '16px' }}>
        
        {/* HEADER */}
        <div style={{ borderBottom: '2px solid #fff7ed', paddingBottom: '20px', marginBottom: '28px' }}>
          <span style={{ fontSize: '12px', fontWeight: '800', color: '#ea580c', textTransform: 'uppercase', letterSpacing: '0.8px' }}>CHÍNH SÁCH DÀNH CHO KHÁCH HÀNG</span>
          <h1 style={{ fontSize: '26px', fontWeight: '900', color: '#0f172a', margin: '6px 0 0 0' }}>
            Chính Sách Đổi Trả & Hoàn Tiền
          </h1>
          <p style={{ fontSize: '14px', color: '#64748b', margin: '6px 0 0 0' }}>
            Cập nhật lần cuối: 2026 - ToyStore cam kết bảo vệ quyền lợi tối đa cho mọi khách hàng.
          </p>
        </div>

        {/* CONTENT SECTIONS */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px', fontSize: '14.5px', color: '#334155', lineHeight: 1.7 }}>
          
          {/* SECTION 1 */}
          <section>
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ background: '#fff7ed', color: '#ea580c', width: '28px', height: '28px', borderRadius: '50%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>1</span>
              Thời Gian & Phạm Vi Áp Dụng
            </h2>
            <p>
              ToyStore hỗ trợ chính sách <strong>đổi trả hàng trong vòng 7 ngày</strong> kể từ thời điểm khách hàng nhận được sản phẩm thành công từ đơn vị vận chuyển.
            </p>
            <ul style={{ paddingLeft: '20px', margin: '8px 0 0 0', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <li>Áp dụng cho toàn bộ đơn hàng mua sắm trực tuyến trên Website và các đại lý chính thức của ToyStore.</li>
              <li>Mỗi đơn hàng được hỗ trợ đổi trả 01 lần với các lý do hợp lệ theo quy định.</li>
            </ul>
          </section>

          {/* SECTION 2 */}
          <section>
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ background: '#fff7ed', color: '#ea580c', width: '28px', height: '28px', borderRadius: '50%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>2</span>
              Điều Kiện Đổi Trả Hàng Hợp Lệ
            </h2>
            <div style={{ background: '#f8fafc', padding: '16px 20px', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
              <ul style={{ paddingLeft: '18px', margin: 0, display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <li>Sản phẩm bị lỗi kỹ thuật từ nhà sản xuất (hỏng hóc linh kiện, thiếu bộ phận,...).</li>
                <li>Sản phẩm bị hư hỏng, bể vỡ hoặc rách bao bì trong quá trình vận chuyển.</li>
                <li>Sản phẩm giao không đúng mẫu mã, màu sắc hoặc chủng loại theo đơn đặt hàng.</li>
                <li>Sản phẩm phải còn nguyên vẹn tem mác, bao bì đính kèm, phụ kiện và quà tặng (nếu có).</li>
              </ul>
            </div>
          </section>

          {/* SECTION 3 */}
          <section>
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ background: '#fff7ed', color: '#ea580c', width: '28px', height: '28px', borderRadius: '50%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>3</span>
              Quy Trình Thực Hiện Đổi Trả (3 Bước Nhanh Chóng)
            </h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '14px', marginTop: '12px' }}>
              <div style={{ background: '#fff8f3', border: '1px solid #fed7aa', padding: '16px', borderRadius: '12px' }}>
                <strong style={{ color: '#ea580c', display: 'block', marginBottom: '4px' }}>Bước 1: Gửi yêu cầu</strong>
                <span style={{ fontSize: '13.5px', color: '#475569' }}>Vào trang "Đơn hàng của tôi" {"->"} Chọn "Yêu cầu trả hàng" hoặc liên hệ Hotline để được tiếp nhận ngay.</span>
              </div>
              <div style={{ background: '#fff8f3', border: '1px solid #fed7aa', padding: '16px', borderRadius: '12px' }}>
                <strong style={{ color: '#ea580c', display: 'block', marginBottom: '4px' }}>Bước 2: Kiểm định QC</strong>
                <span style={{ fontSize: '13.5px', color: '#475569' }}>Gửi sản phẩm về trung tâm bảo hành ToyStore. Chuyên viên QC sẽ kiểm định trong 24 giờ làm việc.</span>
              </div>
              <div style={{ background: '#fff8f3', border: '1px solid #fed7aa', padding: '16px', borderRadius: '12px' }}>
                <strong style={{ color: '#ea580c', display: 'block', marginBottom: '4px' }}>Bước 3: Nhận hàng/Tiền</strong>
                <span style={{ fontSize: '13.5px', color: '#475569' }}>ToyStore gửi lại sản phẩm mới hoặc hoàn lại 100% số tiền qua tài khoản ngân hàng của bạn.</span>
              </div>
            </div>
          </section>

          {/* SECTION 4 */}
          <section>
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ background: '#fff7ed', color: '#ea580c', width: '28px', height: '28px', borderRadius: '50%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>4</span>
              Chi Phí Vận Chuyển Đổi Trả
            </h2>
            <p>
              Nếu lỗi phát sinh từ phía <strong>ToyStore hoặc nhà sản xuất</strong>, ToyStore sẽ <strong>chịu 100% chi phí vận chuyển 2 chiều</strong> cho khách hàng.
            </p>
          </section>

        </div>

        {/* BUTTON */}
        <div style={{ marginTop: '36px', paddingTop: '20px', borderTop: '1px solid #f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: '13px', color: '#94a3b8' }}>Mọi thắc mắc xin liên hệ bộ phận CSKH: 1900 6868</span>
          <Link to="/products" style={{ background: '#ea580c', color: '#fff', padding: '10px 20px', borderRadius: '10px', fontWeight: '800', textDecoration: 'none', fontSize: '13.5px' }}>
            Mua sắm ngay
          </Link>
        </div>

      </div>
    </div>
  );
}

export default ReturnPolicyPage;
