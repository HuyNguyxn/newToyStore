import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCategoryTree } from '../../../services/categoryService.js';

function CategoryMenu() {
  const navigate = useNavigate();
  const [categoryTree, setCategoryTree] = useState([]);
  const [loadError, setLoadError] = useState(false);
  const [stack, setStack] = useState([]);
  const currentParent = stack[stack.length - 1];
  const currentCategories = stack.length === 0 ? categoryTree : getChildren(currentParent);
  const title = stack.length === 0 ? 'Danh mục sản phẩm' : currentParent.name;

  useEffect(() => {
    let active = true;

    getCategoryTree()
      .then((result) => {
        if (active) {
          setCategoryTree(Array.isArray(result) ? result : []);
          setLoadError(false);
        }
      })
      .catch(() => {
        if (active) {
          setCategoryTree([]);
          setLoadError(true);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  function handleCategoryClick(category) {
    if (getChildren(category).length > 0) {
      setStack((current) => [...current, category]);
      return;
    }

    navigate(`/products/category/${category.id}`);
  }

  function goBack() {
    setStack((current) => current.slice(0, -1));
  }

  return (
    <nav className="category-menu" aria-label="Danh mục sản phẩm">
      <div className="category-menu__title">
        <span>Menu</span>
        <span>{title}</span>
      </div>

      {stack.length > 0 && (
        <button className="category-menu__back" type="button" onClick={goBack}>
          ← Quay lại
        </button>
      )}

      <ul className="category-menu__list">
        <li>
          <button type="button" onClick={() => navigate('/products')}>
            <span>Tất cả sản phẩm</span>
          </button>
        </li>

        {loadError && (
          <li>
            <button type="button" disabled>
              <span>Chưa thể tải danh mục</span>
            </button>
          </li>
        )}

        {!loadError && currentCategories.length === 0 && (
          <li>
            <button type="button" disabled>
              <span>Chưa có danh mục con</span>
            </button>
          </li>
        )}

        {currentCategories.map((category) => {
          const childCount = getChildren(category).length;

          return (
            <li key={category.id}>
              <button type="button" onClick={() => handleCategoryClick(category)}>
                <span>{category.name}</span>
                {childCount > 0 && <span>{childCount} ›</span>}
              </button>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

function getChildren(category) {
  return category?.subCategories || category?.children || [];
}

export default CategoryMenu;
