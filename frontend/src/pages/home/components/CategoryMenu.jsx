import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { sampleCategories } from '../../../data/sampleData.js';
import { getCategoryTree } from '../../../services/categoryService.js';

function CategoryMenu() {
  const navigate = useNavigate();
  const [categoryTree, setCategoryTree] = useState([]);
  const [stack, setStack] = useState([]);
  const currentCategories = stack.length === 0 ? categoryTree : getChildren(stack[stack.length - 1]);
  const title = stack.length === 0 ? 'Danh mục sản phẩm' : stack[stack.length - 1].name;

  useEffect(() => {
    let active = true;

    getCategoryTree()
      .then((result) => {
        if (active) {
          setCategoryTree(Array.isArray(result) && result.length > 0 ? result : sampleCategories);
        }
      })
      .catch(() => {
        if (active) {
          setCategoryTree(sampleCategories);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  function handleCategoryClick(category) {
    if (getChildren(category).length > 0) {
      setStack([...stack, category]);
      return;
    }

    navigate(`/products/category/${category.id}`);
  }

  function goBack() {
    setStack(stack.slice(0, -1));
  }

  return (
    <nav className="category-menu" aria-label="Danh mục sản phẩm">
      <div className="category-menu__title">
        <span>Menu</span>
        <span>{title}</span>
      </div>

      {stack.length > 0 && (
        <button className="category-menu__back" type="button" onClick={goBack}>
          Quay lại
        </button>
      )}

      <ul className="category-menu__list">
        {currentCategories.length === 0 && (
          <li>
            <button type="button" onClick={() => navigate('/products')}>
              <span>Xem tất cả sản phẩm</span>
            </button>
          </li>
        )}
        {currentCategories.map((category) => (
          <li key={category.id}>
            <button type="button" onClick={() => handleCategoryClick(category)}>
              <span>{category.name}</span>
              {getChildren(category).length > 0 && <span>&gt;</span>}
            </button>
          </li>
        ))}
      </ul>
    </nav>
  );
}

function getChildren(category) {
  return category?.subCategories || category?.children || [];
}

export default CategoryMenu;
