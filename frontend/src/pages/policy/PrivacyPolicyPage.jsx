import { Link } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';

function PrivacyPolicyPage() {
  return (
    <div className="container" style={{ paddingTop: '24px', paddingBottom: '48px', maxWidth: '900px' }}>
      <BackLink fallback="/" label="Trở về Trang chủ" />

      <div style={{ background: '#ffffff', borderRadius: '20px', padding: '36px 40px', boxShadow: '0 4px 20px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9', marginTop: '16px' }}>
        
        {/* HEADER */}
        <div style={{ borderBottom: '2px solid #fff7ed', paddingBottom: '20px', marginBottom: '28px' }}>
          <span style={{ fontSize: '12px', fontWeight: '800', color: '#ea580c', textTransform: 'uppercase', letterSpacing: '0.8px' }}>BẢO MỆNH AN TOÀN THÔNG TIN</span>
          <h1 style={{ fontSize: '26px', fontWeight: '900', color: '#0f172a', margin: '6px 0 0 0' }}>
            Chính Sách Bảo Mật Thông Tin
          </h1>
          <p style={{ fontSize: '14px', color: '#64748b', margin: '6px 0 0 0' }}>
            Cam kết tôn trọng và bảo vệ quyền riêng tư cá nhân tuyệt đối cho khách hàng khi sử dụng ToyStore.
          </p>
        </div>

        {/* CONTENT SECTIONS */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px', fontSize: '14.5px', color: '#334155', lineHeight: 1.7 }}>
          
          {/* SECTION 1 */}
          <section>
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ background: '#fff7ed', color: '#ea580c', width: '28px', height: '28px', borderRadius: '50%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>1</span>
              Mục Đích Thu Thập Thông Tin
            </h2>
            <p>
              ToyStore thu thập các thông tin cá nhân cơ bản (Họ tên, Email, Số điện thoại, Địa chỉ giao hàng) nhằm các mục đích chính đáng:
            </p>
            <ul style={{ paddingLeft: '20px', margin: '8px 0 0 0', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <li>Xử lý và hoàn tất các đơn đặt hàng hàng ngày của khách hàng.</li>
              <li>Giao hàng nhanh chóng và chính xác thông qua các đối tác vận chuyển liên kết.</li>
              <li>Gửi thông báo cập nhật về trạng thái đơn hàng và hỗ trợ kỹ thuật/kiểm định sản phẩm.</li>
              <li>Gửi các chương trình ưu đãi, mã giảm giá độc quyền nếu được khách hàng đồng ý.</li>
            </ul>
          </section>

          {/* SECTION 2 */}
          <section>
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ background: '#fff7ed', color: '#ea580c', width: '28px', height: '28px', borderRadius: '50%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>2</span>
              Cam Kết Bảo Mật Tuyệt Đối (Không Chia Sẻ)
            </h2>
            <div style={{ background: '#f0fdf4', padding: '16px 20px', borderRadius: '12px', border: '1px solid #bbf7d0' }}>
              <p style={{ margin: 0, color: '#166534', fontWeight: '600' }}>
                ToyStore <strong>cam kết 100% không bán, chia sẻ hay trao đổi thông tin cá nhân của khách hàng</strong> cho bất kỳ bên thứ ba nào khác vì mục đích thương mại. Thông tin chỉ được tiết lộ khi có yêu cầu hợp pháp từ cơ quan pháp luật.
              </p>
            </div>
          </section>

          {/* SECTION 3 */}
          <section>
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ background: '#fff7ed', color: '#ea580c', width: '28px', height: '28px', borderRadius: '50%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>3</span>
              Công Nghệ Mã Hóa Mới Nhất
            </h2>
            <p>
              Toàn bộ dữ liệu mật khẩu và giao dịch thanh toán trực tuyến (VNPay/Momo/Thẻ ngân hàng) đều được bảo vệ bằng giao thức mã hóaSSL/TLS tiêu chuẩn quốc tế. Hệ thống cơ sở dữ liệu của chúng tôi được giám sát an ninh 24/7 để ngăn chặn mọi nguy cơ xâm nhập trái phép.
            </p>
          </section>

          {/* SECTION 4 */}
          <section>
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ background: '#fff7ed', color: '#ea580c', width: '28px', height: '28px', borderRadius: '50%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>4</span>
              Quyền Quản Lý Thông Tin Cá Nhân Của Bạn
            </h2>
            <p>
              Khách hàng có toàn quyền truy cập, kiểm tra, tự chỉnh sửa hoặc yêu cầu ban quản trị ToyStore xóa bỏ dữ liệu thông tin cá nhân của mình bất kỳ lúc nào bằng cách truy cập vào trang <strong>Hồ sơ cá nhân (Profile)</strong> hoặc gửi Email về <code>privacy@toystore.vn</code>.
            </p>
          </section>

        </div>

        {/* BUTTON */}
        <div style={{ marginTop: '36px', paddingTop: '20px', borderTop: '1px solid #f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: '13px', color: '#94a3b8' }}>Trung tâm An toàn Dữ liệu ToyStore Security Center</span>
          <Link to="/products" style={{ background: '#ea580c', color: '#fff', padding: '10px 20px', borderRadius: '10px', fontWeight: '800', textDecoration: 'none', fontSize: '13.5px' }}>
            Khám phá cửa hàng
          </Link>
        </div>

      </div>
    </div>
  );
}

export default PrivacyPolicyPage;
