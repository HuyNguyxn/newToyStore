import { Route, Routes } from 'react-router-dom';
import CustomerLayout from './components/layout/CustomerLayout.jsx';
import HomePage from './pages/home/HomePage.jsx';

function App() {
  return (
    <Routes>
      <Route element={<CustomerLayout />}>
        <Route path="/" element={<HomePage />} />
      </Route>
    </Routes>
  );
}

export default App;
