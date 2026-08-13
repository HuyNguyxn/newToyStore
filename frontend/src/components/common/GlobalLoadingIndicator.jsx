import { useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';

const API_REQUEST_STARTED = 'new-toy-store:api-request-started';
const API_REQUEST_FINISHED = 'new-toy-store:api-request-finished';

function GlobalLoadingIndicator() {
  const location = useLocation();
  const firstRoute = useRef(true);
  const [activeRequests, setActiveRequests] = useState(() => new Set());
  const [routeChanging, setRouteChanging] = useState(false);
  const [waitingPhase, setWaitingPhase] = useState(0);

  useEffect(() => {
    function handleStarted(event) {
      setActiveRequests((current) => {
        const next = new Set(current);
        next.add(event.detail?.requestId);
        return next;
      });
    }

    function handleFinished(event) {
      setActiveRequests((current) => {
        const next = new Set(current);
        next.delete(event.detail?.requestId);
        return next;
      });
    }

    window.addEventListener(API_REQUEST_STARTED, handleStarted);
    window.addEventListener(API_REQUEST_FINISHED, handleFinished);
    return () => {
      window.removeEventListener(API_REQUEST_STARTED, handleStarted);
      window.removeEventListener(API_REQUEST_FINISHED, handleFinished);
    };
  }, []);

  useEffect(() => {
    if (firstRoute.current) {
      firstRoute.current = false;
      return undefined;
    }
    setRouteChanging(true);
    const timer = window.setTimeout(() => setRouteChanging(false), 450);
    return () => window.clearTimeout(timer);
  }, [location.key]);

  useEffect(() => {
    if (activeRequests.size === 0) {
      setWaitingPhase(0);
      return undefined;
    }

    const timers = [
      window.setTimeout(() => setWaitingPhase(1), 600),
      window.setTimeout(() => setWaitingPhase(2), 4000),
      window.setTimeout(() => setWaitingPhase(3), 15000),
    ];
    return () => timers.forEach((timer) => window.clearTimeout(timer));
  }, [activeRequests.size]);

  const isBusy = routeChanging || activeRequests.size > 0;
  const message = waitingPhase === 1
    ? 'Đang tải dữ liệu, vui lòng chờ...'
    : waitingPhase === 2
      ? 'Backend Render đang được đánh thức, dữ liệu sẽ xuất hiện ngay khi máy chủ sẵn sàng.'
      : waitingPhase === 3
        ? 'Render Free có thể cần 30–60 giây để khởi động. Vui lòng giữ nguyên trang.'
        : '';

  return (
    <>
      <div className={`global-loading-bar ${isBusy ? 'is-visible' : ''}`} aria-hidden="true">
        <span />
      </div>
      {message && (
        <div className="global-loading-notice" role="status" aria-live="polite">
          <span className="global-loading-spinner" aria-hidden="true" />
          <div>
            <strong>{waitingPhase >= 2 ? 'Đang kết nối máy chủ' : 'Đang xử lý'}</strong>
            <p>{message}</p>
          </div>
        </div>
      )}
    </>
  );
}

export default GlobalLoadingIndicator;
