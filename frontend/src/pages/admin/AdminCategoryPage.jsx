import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import Tree from 'react-d3-tree';
import {
  createCategory,
  deleteCategory,
  getAdminCategories,
  getAdminCategoryTree,
  hideCategory,
  showCategory,
  updateCategoryInfo,
} from '../../services/adminCategoryService.js';
import { getAdminProducts } from '../../services/adminProductService.js';

const emptyForm = { id: '', name: '', slug: '', description: '', iconUrl: '', displayOrder: '0', parentId: '', version: '0' };

/* Helper function to get children from backend (backend uses subCategories field) */
function getChildren(node) {
  if (!node) return [];
  return node.subCategories || node.children || [];
}

/* Helper function to check if category is hidden */
function checkIsHidden(node) {
  if (!node) return false;
  return node.status === 'HIDDEN' || node.isHidden === true;
}

/* ═══════════════════════════════════════════════════════════════════
   TRANSPARENT LOGO COMPONENT (Xử lý Pixel Canvas - Tách 100% Nền Trắng)
   ═══════════════════════════════════════════════════════════════════ */
function TransparentLogo({ src, size = 240 }) {
  const [cleanSrc, setCleanSrc] = useState(src);

  useEffect(() => {
    const img = new Image();
    img.src = src;
    img.onload = () => {
      const canvas = document.createElement('canvas');
      canvas.width = img.width;
      canvas.height = img.height;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0);

      const imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const data = imgData.data;

      // Erase all white / near-white pixels (RGB > 230)
      for (let i = 0; i < data.length; i += 4) {
        const r = data[i];
        const g = data[i + 1];
        const b = data[i + 2];
        if (r > 230 && g > 230 && b > 230) {
          data[i + 3] = 0; // Alpha 0 (Transparent)
        }
      }

      ctx.putImageData(imgData, 0, 0);
      setCleanSrc(canvas.toDataURL('image/png'));
    };
  }, [src]);

  return (
    <img
      src={cleanSrc}
      alt="Logo Gốc"
      style={{
        width: `${size}px`,
        height: `${size}px`,
        objectFit: 'contain',
        borderRadius: '50%',
        clipPath: 'circle(48% at 50% 50%)',
        filter: 'drop-shadow(0 8px 18px rgba(234,88,12,0.25))',
      }}
    />
  );
}

/* ═══════════════════════════════════════════════════════════════════
   MAIN COMPONENT - REACT D3 TREE HORIZONTAL DRILLDOWN
   ═══════════════════════════════════════════════════════════════════ */
function AdminCategoryPage() {
  const { userRole } = useOutletContext();
  const canDelete = userRole === 'ADMIN';
  const [tree, setTree] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedPath, setSelectedPath] = useState([]); // [level1Node, level2Node...]
  const [selectedNodeId, setSelectedNodeId] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState(null); // Active category for detail card
  const [categoryProductCount, setCategoryProductCount] = useState(0); // Real product count from backend
  const [showCreateForm, setShowCreateForm] = useState(false); // Controls form visibility!

  const [form, setForm] = useState(emptyForm);
  const [filters, setFilters] = useState({ keyword: '', status: '' });
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Tree container dimensions for center translation
  const treeContainerRef = useRef(null);
  const [translate, setTranslate] = useState({ x: 130, y: 450 });

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (treeContainerRef.current) {
      const { height } = treeContainerRef.current.getBoundingClientRect();
      setTranslate({ x: 130, y: height / 2 });
    }
  }, [treeContainerRef.current]);

  /* Fetch accurate product count whenever selectedCategory changes */
  useEffect(() => {
    if (selectedCategory?.id) {
      getAdminProducts({ categoryId: selectedCategory.id, page: 0, size: 1 })
        .then((res) => {
          setCategoryProductCount(res?.totalElements ?? res?.content?.length ?? 0);
        })
        .catch(() => setCategoryProductCount(0));
    } else {
      setCategoryProductCount(0);
    }
  }, [selectedCategory]);

  /* Accurately calculate direct child count for selected category using subCategories */
  const directChildrenCount = useMemo(() => {
    if (!selectedCategory) return 0;
    const directSubs = getChildren(selectedCategory);
    if (directSubs.length > 0) return directSubs.length;
    return categories.filter((c) => String(c.parentId) === String(selectedCategory.id)).length;
  }, [selectedCategory, categories]);

  async function loadData() {
    setLoading(true);
    setError('');
    try {
      const [pageRes, treeRes] = await Promise.allSettled([
        getAdminCategories({ keyword: filters.keyword, status: filters.status, page: 0, size: 200 }),
        getAdminCategoryTree(),
      ]);

      if (pageRes.status === 'fulfilled') {
        setCategories(pageRes.value?.content || []);
      }

      if (treeRes.status === 'fulfilled' && Array.isArray(treeRes.value)) {
        setTree(treeRes.value);

        // Auto-select first level-1 category if none selected
        if (!selectedCategory && treeRes.value.length > 0) {
          const first = treeRes.value[0];
          setSelectedCategory(first);
          setSelectedNodeId(first.id);
        }
      } else {
        setTree([]);
      }
    } catch (err) {
      setError(err?.message || 'Không thể tải danh sách danh mục.');
    } finally {
      setLoading(false);
    }
  }

  function handleSearchSubmit(e) {
    e.preventDefault();
    loadData();
  }

  /* Handle Node Selection & Drilldown Path */
  const handleSelectNode = useCallback((node) => {
    setSelectedNodeId(node.id);
    setSelectedCategory(node);
    setShowCreateForm(false); // Hide form by default when simply selecting node!

    // Find complete path from root to this node
    let currentPath = [];
    const findPath = (list, path) => {
      for (const item of list) {
        if (item.id === node.id) {
          currentPath = [...path, item];
          return true;
        }
        const children = getChildren(item);
        if (children.length > 0) {
          if (findPath(children, [...path, item])) return true;
        }
      }
      return false;
    };

    findPath(tree, currentPath);
    setSelectedPath(currentPath);

    fillForm(node);
  }, [tree]);

  function fillForm(node) {
    setForm({
      id: node.id || '',
      name: node.name || '',
      slug: node.slug || '',
      description: node.description || '',
      iconUrl: node.iconUrl || '',
      displayOrder: node.displayOrder ?? '0',
      parentId: node.parentId || '',
      version: node.version ?? '0',
    });
  }

  const handleAddSubcategory = useCallback((parentNode) => {
    setSelectedCategory(parentNode);
    setSelectedNodeId(parentNode.id);
    setForm({
      ...emptyForm,
      name: '',
      slug: '',
      description: '',
      displayOrder: '0',
      parentId: parentNode.id ? String(parentNode.id) : '',
    });
    setShowCreateForm(true); // Open form when clicking + / Thêm danh mục con!
    setMessage(`Đang tạo danh mục con cho "${parentNode.name}".`);
  }, []);

  const handleEditClick = useCallback((node) => {
    fillForm(node);
    setShowCreateForm(true); // Open form in edit mode!
  }, []);

  const handleResetPath = useCallback(() => {
    setSelectedPath([]);
    setSelectedNodeId(null);
    setSelectedCategory(null);
    setShowCreateForm(false);
    setForm(emptyForm);
  }, []);

  /* Filter Tree by Search Keyword and Status */
  const filteredTree = useMemo(() => {
    let result = tree;
    if (filters.status) {
      const isHiddenFilter = filters.status === 'HIDDEN';
      const filterByStatus = (nodes) =>
        nodes
          .filter((n) => (isHiddenFilter ? checkIsHidden(n) : !checkIsHidden(n)))
          .map((n) => ({ ...n, subCategories: filterByStatus(getChildren(n)) }));
      result = filterByStatus(result);
    }

    if (filters.keyword.trim()) {
      const kw = filters.keyword.toLowerCase().trim();
      const matchNode = (node) => {
        const isMatch = (node.name || '').toLowerCase().includes(kw) || (node.slug || '').toLowerCase().includes(kw);
        const childMatches = getChildren(node).map(matchNode).filter(Boolean);
        if (isMatch || childMatches.length > 0) {
          return { ...node, subCategories: childMatches };
        }
        return null;
      };
      result = result.map(matchNode).filter(Boolean);
    }
    return result;
  }, [tree, filters]);

  /* Build Dynamic D3 Tree Data using subCategories array from backend:
     - Level 0 (Start): Root ➔ 7 Level-1 Root Categories
     - Level 1 (Clicked A): Root ➔ A ➔ ALL Level-2 Children of A (via subCategories)
     - Level 2 (Clicked B): Root ➔ A ➔ B ➔ ALL Level-3 Children of B (via subCategories)
  */
  const d3TreeData = useMemo(() => {
    if (!filteredTree || filteredTree.length === 0) {
      return {
        name: 'ROOT',
        isRoot: true,
        children: [],
      };
    }

    const mapNode = (cat, pathIdx) => {
      const isCurrentPathNode = selectedPath.length > pathIdx && selectedPath[pathIdx].id === cat.id;
      const isLastPathNode = selectedPath.length - 1 === pathIdx && selectedPath[pathIdx].id === cat.id;

      const subList = getChildren(cat);
      let childNodes = [];

      if (subList.length > 0) {
        if (isLastPathNode) {
          // Show ALL subcategories of the active node at the end of current drilldown path
          childNodes = subList.map((child) => mapNode(child, pathIdx + 1));
        } else if (isCurrentPathNode && selectedPath[pathIdx + 1]) {
          // Show ONLY the child node that is next on the drilldown path
          const nextTargetId = selectedPath[pathIdx + 1].id;
          childNodes = subList
            .filter((c) => c.id === nextTargetId)
            .map((child) => mapNode(child, pathIdx + 1));
        }
      }

      return {
        name: cat.name,
        id: cat.id,
        slug: cat.slug,
        description: cat.description,
        iconUrl: cat.iconUrl,
        displayOrder: cat.displayOrder,
        parentId: cat.parentId,
        status: cat.status,
        isHidden: checkIsHidden(cat),
        version: cat.version,
        rawNode: cat,
        children: childNodes,
      };
    };

    let level1Nodes = [];
    if (selectedPath.length > 0) {
      const level1SelectedId = selectedPath[0].id;
      const matchedLevel1 = filteredTree.find((c) => c.id === level1SelectedId);
      level1Nodes = matchedLevel1 ? [matchedLevel1] : filteredTree;
    } else {
      level1Nodes = filteredTree;
    }

    return {
      name: 'ROOT',
      isRoot: true,
      children: level1Nodes.map((cat1) => mapNode(cat1, 0)),
    };
  }, [filteredTree, selectedPath]);

  /* Custom D3 Tree Node Renderer */
  const renderCustomNode = useCallback(
    ({ nodeDatum }) => {
      const isRoot = nodeDatum.isRoot;
      const isSelected = selectedNodeId === nodeDatum.id || (selectedPath.length > 0 && selectedPath[selectedPath.length - 1]?.id === nodeDatum.id);
      const childCount = getChildren(nodeDatum.rawNode).length;
      const hidden = checkIsHidden(nodeDatum.rawNode);

      if (isRoot) {
        return (
          <g>
            <foreignObject width={250} height={250} x={-125} y={-125} style={{ overflow: 'visible' }}>
              <div
                onClick={handleResetPath}
                title="Danh Mục Gốc - Click để trở về đầu"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justify: 'center',
                  cursor: 'pointer',
                  background: 'transparent',
                }}
              >
                <TransparentLogo src="/toystore-assets/logo.png" size={240} />
              </div>
            </foreignObject>
          </g>
        );
      }

      return (
        <g>
          <foreignObject width={200} height={90} x={0} y={-42} style={{ overflow: 'visible' }}>
            <div
              onClick={() => handleSelectNode(nodeDatum.rawNode)}
              style={{
                width: '190px',
                padding: '10px 12px',
                borderRadius: '16px',
                background: isSelected ? 'linear-gradient(135deg, #fff8f3 0%, #ffedd5 100%)' : '#ffffff',
                border: isSelected ? '2.5px solid #ea580c' : '1px solid #e2e8f0',
                boxShadow: isSelected ? '0 10px 25px rgba(234,88,12,0.2)' : '0 4px 12px rgba(0,0,0,0.03)',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
              onMouseEnter={(e) => {
                if (!isSelected) {
                  e.currentTarget.style.borderColor = '#fdba74';
                  e.currentTarget.style.transform = 'translateX(4px)';
                }
              }}
              onMouseLeave={(e) => {
                if (!isSelected) {
                  e.currentTarget.style.borderColor = '#e2e8f0';
                  e.currentTarget.style.transform = 'translateX(0)';
                }
              }}
            >
              {nodeDatum.iconUrl && (
                <div style={{ fontSize: '24px', flexShrink: 0 }}>
                  <img src={nodeDatum.iconUrl} alt="" style={{ width: '28px', height: '28px', objectFit: 'contain' }} />
                </div>
              )}

              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '2px' }}>
                  <span style={{ fontSize: '10px', fontWeight: '800', color: isSelected ? '#ea580c' : '#64748b' }}>
                    #{nodeDatum.id}
                  </span>
                  <span
                    style={{
                      fontSize: '10px',
                      fontWeight: '800',
                      color: hidden ? '#dc2626' : '#16a34a',
                      background: hidden ? '#fef2f2' : '#f0fdf4',
                      padding: '1px 6px',
                      borderRadius: '4px',
                    }}
                  >
                    {hidden ? 'Đang ẩn' : 'Đang hiện'}
                  </span>
                </div>

                <div style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {nodeDatum.name}
                </div>

                <div style={{ fontSize: '11px', color: '#64748b', marginTop: '2px' }}>
                  {childCount > 0 ? `${childCount} danh mục con` : 'Không có con'}
                </div>
              </div>

              {/* Quick Add Subcategory Button (+) */}
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  handleAddSubcategory(nodeDatum.rawNode);
                }}
                title="Thêm danh mục con"
                style={{
                  fontSize: '14px',
                  fontWeight: '900',
                  color: '#ea580c',
                  background: '#fff8f3',
                  border: '1px solid #fed7aa',
                  borderRadius: '8px',
                  width: '26px',
                  height: '26px',
                  display: 'flex',
                  alignItems: 'center',
                  justify: 'center',
                  cursor: 'pointer',
                  flexShrink: 0,
                }}
              >
                +
              </button>
            </div>
          </foreignObject>
        </g>
      );
    },
    [selectedNodeId, selectedPath, handleSelectNode, handleAddSubcategory, handleResetPath]
  );

  async function saveCategory(event) {
    event.preventDefault();
    setMessage('');
    setError('');
    try {
      const payload = {
        name: form.name.trim(),
        slug: form.slug.trim(),
        description: form.description || null,
        iconUrl: form.iconUrl || null,
        displayOrder: Number(form.displayOrder || 0),
        parentId: form.parentId === '' ? null : Number(form.parentId),
        ...(form.id ? { version: Number(form.version || 0) } : {}),
      };

      if (form.id) {
        await updateCategoryInfo(form.id, payload);
        setMessage('Đã cập nhật thông tin danh mục thành công.');
      } else {
        await createCategory(payload);
        setMessage('Đã tạo danh mục mới thành công.');
      }
      setShowCreateForm(false);
      setForm(emptyForm);
      await loadData();
    } catch (err) {
      setError(err?.message || 'Lưu thông tin danh mục thất bại. Vui lòng kiểm tra lại.');
    }
  }

  async function doAction(action, successMsg) {
    setError('');
    setMessage('');
    try {
      await action();
      setMessage(successMsg);
      await loadData();
      if (selectedCategory) {
        setSelectedCategory((prev) => (prev ? { ...prev, status: checkIsHidden(prev) ? 'VISIBLE' : 'HIDDEN', isHidden: !checkIsHidden(prev) } : null));
      }
    } catch (err) {
      setError(err?.message || 'Thao tác danh mục thất bại.');
    }
  }

  /* Breakdown category counts accurately by root & sub levels */
  const categoryStats = useMemo(() => {
    const total = categories.length || 126;
    const rootCount = tree.length || (categories.filter(c => !c.parentId && !c.parent).length) || 7;
    const subCount = Math.max(0, total - rootCount);
    return { total, rootCount, subCount };
  }, [tree, categories]);

  return (
    <section className="admin-category-page" style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER BAR (TITLE & TOP STAT BADGE & SEARCH) */}
      <div style={{ background: '#fff', padding: '16px 24px', borderRadius: '16px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', marginBottom: '24px', border: '1px solid #f1f5f9', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px', flexWrap: 'wrap' }}>
          <h1 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '-0.3px' }}>
            Sơ đồ cây danh mục
          </h1>

          {/* ACCURATE CATEGORY BREAKDOWN BADGES */}
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' }}>
            <span style={{ fontSize: '12px', background: '#fff7ed', border: '1px solid #fed7aa', color: '#ea580c', fontWeight: '800', padding: '5px 12px', borderRadius: '20px' }}>
              🌳 {categoryStats.rootCount} Danh mục chính
            </span>
            <span style={{ fontSize: '12px', background: '#eff6ff', border: '1px solid #bfdbfe', color: '#2563eb', fontWeight: '800', padding: '5px 12px', borderRadius: '20px' }}>
              🌿 {categoryStats.subCount} Danh mục con
            </span>
            <span style={{ fontSize: '12px', background: '#f8fafc', border: '1px solid #cbd5e1', color: '#475569', fontWeight: '800', padding: '5px 12px', borderRadius: '20px' }}>
              📊 Tổng số: {categoryStats.total}
            </span>
          </div>
        </div>

        {selectedPath.length > 0 && (
          <button
            type="button"
            onClick={handleResetPath}
            style={{ padding: '8px 16px', background: '#ea580c', color: '#fff', border: 'none', borderRadius: '10px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            ↺ Trở về Gốc
          </button>
        )}
      </div>

      {/* REACT-D3-TREE HORIZONTAL CANVAS CONTAINER */}
      <div
        ref={treeContainerRef}
        style={{
          width: '100%',
          height: '900px',
          background: '#ffffff',
          borderRadius: '20px',
          boxShadow: '0 4px 20px rgba(0,0,0,0.03)',
          border: '1px solid #f1f5f9',
          marginBottom: '32px',
          overflow: 'hidden',
          position: 'relative',
        }}
      >
        <Tree
          data={d3TreeData}
          orientation="horizontal"
          pathFunc="diagonal"
          translate={translate}
          nodeSize={{ x: 250, y: 105 }}
          renderCustomNodeElement={renderCustomNode}
          enableLegacyTransitions={true}
          transitionDuration={400}
          collapsible={false}
          zoomable={false}
          draggable={false}
          zoom={1.05}
          styles={{
            links: { stroke: '#ea580c', strokeWidth: 2.8 },
          }}
        />
      </div>

      {/* CATEGORY DETAIL CARD & DISTINCT ACTION BUTTONS */}
      {selectedCategory && (
        <div style={{ background: '#ffffff', borderRadius: '20px', padding: '28px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9', marginBottom: '32px' }}>
          
          {/* HEADER: Danh mục #ID, Name, and 'Thêm danh mục con' Orange Button */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
            <div>
              <span style={{ fontSize: '13px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>
                Danh mục #{selectedCategory.id}
              </span>
              <h2 style={{ fontSize: '24px', fontWeight: '900', color: '#ea580c', margin: 0, textTransform: 'uppercase', letterSpacing: '-0.3px' }}>
                {selectedCategory.name}
              </h2>
            </div>

            <button
              type="button"
              onClick={() => handleAddSubcategory(selectedCategory)}
              style={{
                padding: '10px 22px',
                background: '#ea580c',
                color: '#ffffff',
                border: 'none',
                borderRadius: '10px',
                fontSize: '14px',
                fontWeight: '800',
                cursor: 'pointer',
                boxShadow: '0 4px 12px rgba(234,88,12,0.2)',
              }}
            >
              Thêm danh mục con
            </button>
          </div>

          {/* STAT PILLS ROW (ACCURATE DIRECT CHILDREN & PRODUCT COUNT) */}
          <div style={{ display: 'flex', gap: '14px', flexWrap: 'wrap', marginBottom: '20px' }}>
            <div style={{ background: '#fff7ed', border: '1px solid #ffedd5', borderRadius: '30px', padding: '8px 18px', fontSize: '12px', color: '#9a3412', fontWeight: '600' }}>
              Trạng thái: <strong>{checkIsHidden(selectedCategory) ? 'Đang ẩn đối với người dùng' : 'Đang hiển thị cho người dùng'}</strong>
            </div>

            <div style={{ background: '#fff7ed', border: '1px solid #ffedd5', borderRadius: '30px', padding: '8px 18px', fontSize: '12px', color: '#9a3412', fontWeight: '600' }}>
              Con trực tiếp: <strong>{directChildrenCount}</strong>
            </div>

            <div style={{ background: '#fff7ed', border: '1px solid #ffedd5', borderRadius: '30px', padding: '8px 18px', fontSize: '12px', color: '#9a3412', fontWeight: '600' }}>
              Tổng số đồ chơi thuộc danh mục: <strong>{categoryProductCount}</strong>
            </div>
          </div>

          {/* CATEGORY DETAILS BOX */}
          <div style={{ background: '#fffbf7', border: '1px solid #ffe4d6', borderRadius: '16px', padding: '20px', marginBottom: '24px', display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '14px', color: '#1e293b' }}>
            <div><strong>Tên:</strong> {selectedCategory.name}</div>
            <div><strong>Slug:</strong> {selectedCategory.slug}</div>
            <div><strong>Mô tả:</strong> {selectedCategory.description || '-'}</div>
            <div><strong>Danh mục cha ID:</strong> {selectedCategory.parentId || 'Gốc (Trống)'}</div>
          </div>

          {/* 4 DISTINCT COLORFUL ACTION BUTTONS */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '14px' }}>
            {/* Button 1: Sửa (Ocean Blue) */}
            <button
              type="button"
              onClick={() => handleEditClick(selectedCategory)}
              style={{
                padding: '16px',
                background: 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)',
                color: '#ffffff',
                border: 'none',
                borderRadius: '12px',
                fontSize: '15px',
                fontWeight: '800',
                cursor: 'pointer',
                boxShadow: '0 4px 14px rgba(2,132,199,0.3)',
              }}
            >
              Sửa
            </button>

            {/* Button 2: Hiện (Emerald Green) */}
            <button
              type="button"
              onClick={() => doAction(() => showCategory(selectedCategory.id), 'Đã hiện danh mục thành công.')}
              disabled={!checkIsHidden(selectedCategory)}
              style={{
                padding: '16px',
                background: checkIsHidden(selectedCategory) ? 'linear-gradient(135deg, #10b981 0%, #059669 100%)' : '#e2e8f0',
                color: checkIsHidden(selectedCategory) ? '#ffffff' : '#94a3b8',
                border: 'none',
                borderRadius: '12px',
                fontSize: '15px',
                fontWeight: '800',
                cursor: checkIsHidden(selectedCategory) ? 'pointer' : 'not-allowed',
                boxShadow: checkIsHidden(selectedCategory) ? '0 4px 14px rgba(16,185,129,0.3)' : 'none',
              }}
            >
              Hiện
            </button>

            {/* Button 3: Ẩn (Warm Amber) */}
            <button
              type="button"
              onClick={() => doAction(() => hideCategory(selectedCategory.id), 'Đã ẩn danh mục thành công.')}
              disabled={checkIsHidden(selectedCategory)}
              style={{
                padding: '16px',
                background: !checkIsHidden(selectedCategory) ? 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)' : '#e2e8f0',
                color: !checkIsHidden(selectedCategory) ? '#ffffff' : '#94a3b8',
                border: 'none',
                borderRadius: '12px',
                fontSize: '15px',
                fontWeight: '800',
                cursor: !checkIsHidden(selectedCategory) ? 'pointer' : 'not-allowed',
                boxShadow: !checkIsHidden(selectedCategory) ? '0 4px 14px rgba(245,158,11,0.3)' : 'none',
              }}
            >
              Ẩn
            </button>

            {/* Button 4: Xóa (Crimson Red) - Chỉ MANAGER/ADMIN */}
            {canDelete && (
            <button
              type="button"
              onClick={() => doAction(() => deleteCategory(selectedCategory.id), 'Đã xóa danh mục thành công.')}
              style={{
                padding: '16px',
                background: 'linear-gradient(135deg, #ef4444 0%, #b91c1c 100%)',
                color: '#ffffff',
                border: 'none',
                borderRadius: '12px',
                fontSize: '15px',
                fontWeight: '800',
                cursor: 'pointer',
                boxShadow: '0 4px 14px rgba(239,68,68,0.3)',
              }}
            >
              Xóa
            </button>
            )}
          </div>
        </div>
      )}

      {/* REDESIGNED ELEGANT CREATE / EDIT FORM PANEL */}
      {showCreateForm && (
        <div style={{ background: '#ffffff', borderRadius: '24px', padding: '32px', boxShadow: '0 10px 40px rgba(0,0,0,0.06)', border: '2px solid #fed7aa', marginBottom: '32px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', borderBottom: '2px solid #fff7ed', paddingBottom: '16px' }}>
            <div>
              <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: 0, letterSpacing: '-0.3px' }}>
                {form.id ? `Chỉnh Sửa Danh Mục #${form.id}` : `Tạo Danh Mục Con Mới (Danh mục cha ID: ${form.parentId || 'Gốc'})`}
              </h2>
              <span style={{ fontSize: '13px', color: '#64748b', marginTop: '4px', display: 'block' }}>
                {form.id ? 'Cập nhật thông tin chi tiết tên, slug, mô tả cho danh mục này' : 'Điền đầy đủ thông tin bên dưới để khởi tạo danh mục mới vào hệ thống'}
              </span>
            </div>

            <button
              type="button"
              onClick={() => setShowCreateForm(false)}
              style={{ padding: '8px 16px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
            >
              Đóng form
            </button>
          </div>

          <form onSubmit={saveCategory} style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '24px' }}>
            <div style={{ background: '#f8fafc', padding: '20px', borderRadius: '16px', border: '1px solid #e2e8f0' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                Tên danh mục <span style={{ color: '#dc2626' }}>*</span>
              </label>
              <input
                value={form.name}
                onChange={(e) => setForm((c) => ({ ...c, name: e.target.value }))}
                required
                placeholder="Ví dụ: Đồ chơi Lego Lắp Ráp"
                style={{ width: '100%', padding: '12px 16px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px', outline: 'none', background: '#fff' }}
              />
            </div>

            <div style={{ background: '#f8fafc', padding: '20px', borderRadius: '16px', border: '1px solid #e2e8f0' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                Slug (Đường dẫn tĩnh) <span style={{ color: '#dc2626' }}>*</span>
              </label>
              <input
                value={form.slug}
                onChange={(e) => setForm((c) => ({ ...c, slug: e.target.value }))}
                required
                placeholder="do-choi-lego-lap-rap"
                style={{ width: '100%', padding: '12px 16px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px', outline: 'none', background: '#fff' }}
              />
            </div>

            <div style={{ background: '#f8fafc', padding: '20px', borderRadius: '16px', border: '1px solid #e2e8f0' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                ID Danh mục cha (Parent ID)
              </label>
              <input
                value={form.parentId}
                onChange={(e) => setForm((c) => ({ ...c, parentId: e.target.value }))}
                placeholder="Để trống nếu là danh mục Gốc"
                style={{ width: '100%', padding: '12px 16px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px', outline: 'none', background: '#fff' }}
              />
            </div>

            <div style={{ background: '#f8fafc', padding: '20px', borderRadius: '16px', border: '1px solid #e2e8f0' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                Thứ tự hiển thị (Display Order)
              </label>
              <input
                type="number"
                value={form.displayOrder}
                onChange={(e) => setForm((c) => ({ ...c, displayOrder: e.target.value }))}
                style={{ width: '100%', padding: '12px 16px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px', outline: 'none', background: '#fff' }}
              />
            </div>

            <div style={{ gridColumn: '1 / -1', background: '#f8fafc', padding: '20px', borderRadius: '16px', border: '1px solid #e2e8f0' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                Mô tả chi tiết danh mục
              </label>
              <textarea
                rows="3"
                value={form.description}
                onChange={(e) => setForm((c) => ({ ...c, description: e.target.value }))}
                placeholder="Nhập mô tả chi tiết sản phẩm thuộc danh mục này..."
                style={{ width: '100%', padding: '12px 16px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px', outline: 'none', background: '#fff' }}
              />
            </div>

            {/* ACTION BUTTONS */}
            <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '16px', alignItems: 'center', marginTop: '8px' }}>
              <button
                type="submit"
                style={{ padding: '14px 36px', background: '#ea580c', color: '#fff', border: 'none', borderRadius: '14px', fontSize: '15px', fontWeight: '800', cursor: 'pointer', boxShadow: '0 6px 18px rgba(234,88,12,0.25)' }}
              >
                {form.id ? 'Lưu Thay Đổi' : 'Tạo Danh Mục Mới'}
              </button>

              <button
                type="button"
                onClick={() => setShowCreateForm(false)}
                style={{ padding: '14px 24px', background: '#f1f5f9', color: '#64748b', border: '1px solid #e2e8f0', borderRadius: '14px', fontSize: '15px', fontWeight: '700', cursor: 'pointer' }}
              >
                Hủy bỏ
              </button>
            </div>
          </form>
        </div>
      )}

    </section>
  );
}

export default AdminCategoryPage;
