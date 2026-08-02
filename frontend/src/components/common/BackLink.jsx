import { Link, useNavigate } from 'react-router-dom';

function BackLink({ fallback = '/', label = 'Quay lại' }) {
  const navigate = useNavigate();

  function handleBack(event) {
    event.preventDefault();

    if (window.history.length > 1) {
      navigate(-1);
      return;
    }

    navigate(fallback);
  }

  return (
    <Link to={fallback} className="back-link" onClick={handleBack}>
      <span aria-hidden="true">←</span>
      {label}
    </Link>
  );
}

export default BackLink;
