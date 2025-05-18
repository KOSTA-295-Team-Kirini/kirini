/**
 * 키보드 스펙 시각화 관련 스크립트
 */

document.addEventListener('DOMContentLoaded', function() {
  const keyboardId = new URLSearchParams(window.location.search).get('id');
  
  if (keyboardId) {
    // 스펙 시각화 컴포넌트 초기화
    initSpecVisualizations();
  }
});

/**
 * 스펙 시각화 컴포넌트 초기화
 */
function initSpecVisualizations() {
  // 스펙 시각화 섹션 동적 생성
  createSpecVisualizationSection();
  
  // 애니메이션 설정 (스크롤 이벤트 연결)
  setupSpecAnimations();
}

/**
 * 스펙 시각화 섹션 생성
 */
function createSpecVisualizationSection() {
  // 기존 스펙 정보에서 데이터 추출
  const specData = extractSpecData();
  
  // 컨테이너 찾기
  const detailContainer = document.querySelector('.detail-container');
  if (!detailContainer) return;
  
  // 탭 콘텐츠 내부에 시각화 섹션 추가
  const descriptionTab = document.getElementById('tab-description');
  
  if (descriptionTab) {
    const specVizSection = document.createElement('div');
    specVizSection.className = 'spec-visualization';
    specVizSection.innerHTML = `
      <h3 class="spec-viz-title">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 8v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2Z"></path>
          <path d="M23 11h1"></path>
          <path d="M0 11h1"></path>
          <path d="M8 9v5"></path>
          <path d="M12 9v5"></path>
          <path d="M16 9v5"></path>
        </svg>
        키보드 스펙 시각화
      </h3>
      <div class="spec-grid">
        ${createSpecCards(specData)}
      </div>
    `;
    
    descriptionTab.appendChild(specVizSection);
  }
}

/**
 * 스펙 정보 추출
 */
function extractSpecData() {
  const specItems = Array.from(document.querySelectorAll('.spec-item'));
  
  // 스펙 데이터 추출
  return specItems.map(item => {
    const label = item.querySelector('.spec-label')?.textContent || '';
    const value = item.querySelector('.spec-value')?.textContent || '';
    
    return { label, value };
  });
}

/**
 * 스펙 카드 생성
 */
function createSpecCards(specData) {
  if (!specData || !specData.length) return '';
  
  const cards = [];
  
  // 타입 카드 (아이콘형)
  const typeSpec = specData.find(spec => spec.label.includes('타입'));
  if (typeSpec) {
    cards.push(createTypeCard(typeSpec));
  }
  
  // 스위치 카드 (슬라이더형)
  const switchSpec = specData.find(spec => spec.label.includes('스위치'));
  if (switchSpec) {
    cards.push(createSwitchCard(switchSpec));
  }
  
  // 폴링 레이트 카드 (도넛 차트형)
  const pollingRateSpec = specData.find(spec => spec.label.includes('폴링'));
  if (pollingRateSpec) {
    cards.push(createPollingRateCard(pollingRateSpec));
  }
  
  // RGB 카드 (바 차트형)
  const rgbSpec = specData.find(spec => spec.label.includes('RGB'));
  if (rgbSpec) {
    cards.push(createRgbCard(rgbSpec));
  }

  // 크기 및 무게 카드 (비교형)
  const sizeSpec = specData.find(spec => spec.label.includes('크기'));
  const weightSpec = specData.find(spec => spec.label.includes('무게'));
  if (sizeSpec || weightSpec) {
    cards.push(createSizeWeightCard(sizeSpec, weightSpec));
  }
  
  return cards.join('');
}

/**
 * 타입 카드 생성 (아이콘형)
 */
function createTypeCard(spec) {
  const types = spec.value.split(',').map(type => type.trim());
  
  // 기계식, 무접점, 멤브레인 타입을 아이콘으로 표시
  const typeIcons = {
    '기계식': '<span class="material-icons">keyboard</span>',
    '무접점': '<span class="material-icons">sensors</span>',
    '멤브레인': '<span class="material-icons">grid_3x3</span>',
    '텐키리스': '<span class="material-icons">width_normal</span>',
    'TKL': '<span class="material-icons">width_normal</span>',
    '풀사이즈': '<span class="material-icons">width_full</span>',
    '60%': '<span class="material-icons">width_wide</span>'
  };
  
  const typeDisplay = types.map(type => {
    const icon = Object.keys(typeIcons).find(key => type.includes(key));
    const iconHtml = icon ? typeIcons[icon] : '<span class="material-icons">keyboard</span>';
    
    return `
      <div class="spec-type-item">
        ${iconHtml}
        <span>${type}</span>
      </div>
    `;
  }).join('');
  
  return `
    <div class="spec-card">
      <div class="spec-card-title">
        <span class="spec-icon material-icons">keyboard</span>
        키보드 타입
      </div>
      <div class="spec-type-grid">
        ${typeDisplay}
      </div>
    </div>
  `;
}

/**
 * 스위치 카드 생성 (슬라이더형)
 */
function createSwitchCard(spec) {
  // 스위치 작동점 추출 (숫자가 있는 경우)
  const actuation = spec.value.match(/(\d+(\.\d+)?)\s*mm/);
  
  let actValue = 0;
  let actRange = '';
  
  // 작동점 범위 추출 (예: 1.5mm ~ 3.8mm)
  const actRange2 = spec.value.match(/(\d+(\.\d+)?)\s*mm\s*~\s*(\d+(\.\d+)?)\s*mm/);
  
  if (actRange2) {
    actValue = (parseFloat(actRange2[1]) + parseFloat(actRange2[3])) / 2;
    actRange = `${actRange2[1]}mm ~ ${actRange2[3]}mm`;
  } else if (actuation) {
    actValue = parseFloat(actuation[1]);
    actRange = `0mm ~ 4mm`;
  } else {
    actValue = 2.0;
    actRange = `0mm ~ 4mm`;
  }
  
  // 슬라이더 비율 계산 (보통 작동점은 1mm ~ 4mm 범위)
  const percentage = Math.min(Math.max((actValue - 1) / 3 * 100, 0), 100);
  
  return `
    <div class="spec-card switch-card">
      <div class="spec-card-title">
        <span class="spec-icon material-icons">touch_app</span>
        스위치
      </div>
      <div class="spec-switch-name">${spec.value}</div>
      <div class="spec-slider">
        <div class="spec-slider-value" style="width: ${percentage}%"></div>
      </div>
      <div class="spec-slider-labels">
        <span>가벼움 (1mm)</span>
        <span>깊음 (4mm)</span>
      </div>
      ${actRange ? `<div class="spec-actuation-range">작동점: ${actRange}</div>` : ''}
    </div>
  `;
}

/**
 * 폴링 레이트 카드 생성 (도넛 차트형)
 */
function createPollingRateCard(spec) {
  // 폴링 레이트 숫자 추출 (Hz)
  const pollingRate = spec.value.match(/(\d+)Hz/) || spec.value.match(/(\d+,\d+)Hz/);
  
  let pollingValue = 1000;
  if (pollingRate) {
    pollingValue = parseInt(pollingRate[1].replace(/,/g, ''));
  }
  
  // 퍼센티지 계산 (최대 8,000Hz 기준)
  const max = 8000;
  const percentage = Math.min((pollingValue / max) * 100, 100);
  
  // 도넛 차트 회전 각도 계산
  const degrees = (percentage / 100) * 360;
  
  return `
    <div class="spec-card polling-card">
      <div class="spec-card-title">
        <span class="spec-icon material-icons">speed</span>
        폴링 레이트
      </div>
      <div class="spec-donut">
        <div class="spec-donut-ring"></div>
        <div class="spec-donut-segment ${degrees > 180 ? 'show-more' : ''}">
          <div class="spec-donut-fill" style="transform: rotate(${Math.min(180, degrees)}deg)"></div>
        </div>
        ${degrees > 180 ? `
          <div class="spec-donut-segment" style="clip: rect(0px, 60px, 120px, 0px)">
            <div class="spec-donut-fill" style="transform: rotate(${degrees - 180}deg)"></div>
          </div>
        ` : ''}
        <div class="spec-donut-center">${pollingValue}Hz</div>
      </div>
      <div class="spec-polling-desc">응답 속도: ${Math.round(1000 / pollingValue * 100) / 100}ms</div>
    </div>
  `;
}

/**
 * RGB 카드 생성 (바 차트형)
 */
function createRgbCard(spec) {
  // RGB 기능 목록 추출
  const features = [
    { name: '프로그래밍', value: spec.value.includes('프로그래밍') ? 100 : 0 },
    { name: '풀 RGB', value: spec.value.includes('풀') ? 100 : (spec.value.includes('RGB') ? 70 : 0) },
    { name: '게임 연동', value: spec.value.includes('게임') || spec.value.includes('연동') ? 100 : 0 },
    { name: '다양한 효과', value: spec.value.includes('효과') ? 90 : (spec.value.includes('모드') ? 70 : 0) }
  ];
  
  const featureBars = features.map(feature => `
    <div class="spec-bar-item">
      <div class="spec-bar-label">${feature.name}</div>
      <div class="spec-bar-container">
        <div class="spec-bar-value" data-value="${feature.value}"></div>
      </div>
      <div class="spec-bar-text">${feature.value > 0 ? '✓' : '✗'}</div>
    </div>
  `).join('');
  
  return `
    <div class="spec-card rgb-card">
      <div class="spec-card-title">
        <span class="spec-icon material-icons">wb_sunny</span>
        RGB 기능
      </div>
      <div class="spec-bars">
        ${featureBars}
      </div>
    </div>
  `;
}

/**
 * 크기 및 무게 카드 생성 (비교형)
 */
function createSizeWeightCard(sizeSpec, weightSpec) {
  let sizeHtml = '';
  let weightHtml = '';
  
  if (sizeSpec) {
    // 크기 정보 추출 (가로 x 세로 x 높이)
    const dimensions = sizeSpec.value.match(/(\d+(\.\d+)?)\s*x\s*(\d+(\.\d+)?)\s*x\s*(\d+(\.\d+)?)/);
    
    if (dimensions) {
      const width = parseFloat(dimensions[1]);
      const depth = parseFloat(dimensions[3]);
      const height = parseFloat(dimensions[5]);
      
      // 일반적인 키보드 크기와 비교 (가로 기준, 450mm)
      const widthPercentage = Math.min((width / 450) * 100, 100);
      
      sizeHtml = `
        <div class="spec-size-info">
          <div class="spec-size-label">크기</div>
          <div class="spec-size-value">${sizeSpec.value}</div>
          <div class="spec-size-visual">
            <div class="spec-size-reference">일반 풀사이즈 대비</div>
            <div class="spec-size-bar">
              <div class="spec-size-value-bar" data-value="${widthPercentage}"></div>
            </div>
            <div class="spec-size-percentage">${Math.round(widthPercentage)}%</div>
          </div>
        </div>
      `;
    }
  }
  
  if (weightSpec) {
    // 무게 추출 (g 또는 kg)
    const weight = weightSpec.value.match(/(\d+(\.\d+)?)\s*g/) || weightSpec.value.match(/(\d+(\.\d+)?)\s*kg/);
    
    if (weight) {
      let weightValue = parseFloat(weight[1]);
      const unit = weightSpec.value.includes('kg') ? 'kg' : 'g';
      
      // kg 단위를 g 단위로 변환
      if (unit === 'kg') {
        weightValue *= 1000;
      }
      
      // 일반적인 키보드 무게와 비교 (1000g)
      const weightPercentage = Math.min((weightValue / 1000) * 100, 120);
      
      weightHtml = `
        <div class="spec-weight-info">
          <div class="spec-weight-label">무게</div>
          <div class="spec-weight-value">${weightSpec.value}</div>
          <div class="spec-weight-visual">
            <div class="spec-icon-label">무게감</div>
            <div class="spec-icons">
              ${Array(5).fill().map((_, i) => 
                `<span class="material-icons spec-icon-item ${i < Math.ceil(weightPercentage / 24) ? 'filled' : ''}">fitness_center</span>`
              ).join('')}
            </div>
          </div>
        </div>
      `;
    }
  }
  
  return `
    <div class="spec-card size-weight-card">
      <div class="spec-card-title">
        <span class="spec-icon material-icons">straighten</span>
        크기 및 무게
      </div>
      ${sizeHtml}
      ${weightHtml}
    </div>
  `;
}

/**
 * 시각화 애니메이션 설정
 */
function setupSpecAnimations() {
  // Intersection Observer를 사용하여 요소가 뷰포트에 들어올 때 애니메이션 적용
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        // 요소가 화면에 보이면 애니메이션 적용
        animateSpecVisualizations(entry.target);
        
        // 한번 애니메이션이 실행되면 더 이상 관찰하지 않음
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.2 });
  
  // 시각화 요소들 관찰 등록
  const specViz = document.querySelector('.spec-visualization');
  if (specViz) {
    observer.observe(specViz);
  }
}

/**
 * 스펙 시각화 애니메이션 적용
 */
function animateSpecVisualizations(container) {
  // 도넛 차트 애니메이션
  const donutFills = container.querySelectorAll('.spec-donut-fill');
  donutFills.forEach(fill => {
    const rotation = fill.style.transform.match(/rotate\((\d+)deg\)/);
    if (rotation) {
      const degrees = parseInt(rotation[1]);
      fill.style.transition = 'transform 1.5s ease-out';
    }
  });
  
  // 바 차트 애니메이션
  const barValues = container.querySelectorAll('.spec-bar-value');
  barValues.forEach((bar, index) => {
    setTimeout(() => {
      const value = bar.getAttribute('data-value');
      bar.style.width = `${value}%`;
    }, index * 150);
  });
  
  // 크기 바 애니메이션
  const sizeBars = container.querySelectorAll('.spec-size-value-bar');
  sizeBars.forEach((bar, index) => {
    setTimeout(() => {
      const value = bar.getAttribute('data-value');
      bar.style.width = `${value}%`;
    }, 300);
  });
  
  // 아이콘 애니메이션
  const iconItems = container.querySelectorAll('.spec-icon-item');
  iconItems.forEach((icon, index) => {
    setTimeout(() => {
      icon.style.opacity = '1';
      icon.style.transform = 'scale(1)';
    }, index * 100);
  });
}
