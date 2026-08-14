import { Link, useLocation, useNavigate } from 'react-router-dom';

function BackLink({ fallback = '/', label = 'Quay lại', className = '' }) {
  const location = useLocation();
  const navigate = useNavigate();

  function handleBack(event) {
    event.preventDefault();

    if (location.key !== 'default' && window.history.length > 1) {
      navigate(-1);
      return;
    }

    navigate(fallback);
  }

  return (
    <Link to={fallback} className={`back-link ${className}`.trim()} onClick={handleBack}>
      <span aria-hidden="true">←</span>
      {label}
    </Link>
  );
}

export default BackLink;
