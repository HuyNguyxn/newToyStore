import { useState } from 'react';

const categoryTree = [
  {
    id: 1,
    name: 'Do choi lap rap',
    children: [
      { id: 11, name: 'LEGO', children: [] },
      { id: 12, name: 'Khoi xay dung', children: [] },
      { id: 13, name: 'Lap rap co khi', children: [] },
    ],
  },
  {
    id: 2,
    name: 'Phuong tien do choi',
    children: [
      { id: 21, name: 'Xe dieu khien', children: [] },
      { id: 22, name: 'May bay mo hinh', children: [] },
      { id: 23, name: 'Tau thuyen', children: [] },
    ],
  },
  {
    id: 3,
    name: 'Do choi giao duc',
    children: [
      { id: 31, name: 'Hoc chu cai', children: [] },
      { id: 32, name: 'Do choi STEM', children: [] },
    ],
  },
  { id: 4, name: 'Thu bong', children: [] },
  { id: 5, name: 'Mo hinh', children: [] },
];

function CategoryMenu() {
  const [stack, setStack] = useState([]);
  const currentCategories = stack.length === 0 ? categoryTree : stack[stack.length - 1].children;
  const title = stack.length === 0 ? 'Danh muc san pham' : stack[stack.length - 1].name;

  function handleCategoryClick(category) {
    if (category.children && category.children.length > 0) {
      setStack([...stack, category]);
      return;
    }

    window.location.href = `/products/category/${category.id}`;
  }

  function goBack() {
    setStack(stack.slice(0, -1));
  }

  return (
    <nav className="category-menu" aria-label="Danh muc san pham">
      <div className="category-menu__title">
        <span>☰</span>
        <span>{title}</span>
      </div>

      {stack.length > 0 && (
        <button className="category-menu__back" type="button" onClick={goBack}>
          ‹ Quay lai
        </button>
      )}

      <ul className="category-menu__list">
        {currentCategories.map((category) => (
          <li key={category.id}>
            <button type="button" onClick={() => handleCategoryClick(category)}>
              <span>{category.name}</span>
              {category.children && category.children.length > 0 && <span>›</span>}
            </button>
          </li>
        ))}
      </ul>
    </nav>
  );
}

export default CategoryMenu;
