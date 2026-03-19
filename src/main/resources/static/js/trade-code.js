(function () {
  'use strict';

  document.addEventListener('click', function (e) {
    const btn = e.target.closest('.copy-btn');
    if (!btn) return;

    const tradeCode = btn.dataset.tradeCode || '';
    // Find the associated fallback input (next sibling .trade-fallback)
    const fallback = btn.parentElement.querySelector('.trade-fallback');

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(tradeCode)
        .then(function () {
          showCopied(btn);
        })
        .catch(function () {
          showFallback(fallback, tradeCode);
        });
    } else {
      // Clipboard API not supported
      showFallback(fallback, tradeCode);
    }
  });

  function showCopied(btn) {
    const original = btn.textContent;
    btn.textContent = '✅ 已複製！';
    btn.disabled = true;
    setTimeout(function () {
      btn.textContent = original;
      btn.disabled = false;
    }, 2000);
  }

  function showFallback(input, text) {
    if (!input) return;
    input.value = text;
    input.style.display = '';
    input.focus();
    input.select();
  }
})();
