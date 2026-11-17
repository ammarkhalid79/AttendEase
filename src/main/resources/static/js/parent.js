const markAll = document.querySelector("[data-mark-all]");
if (markAll) {
    markAll.addEventListener("click", () => {
        document.querySelectorAll(".mark-select").forEach(select => {
            select.value = "PRESENT";
        });
        document.querySelector(".mark-select")?.focus();
    });
}

document.querySelectorAll("form[data-confirm]").forEach(form => {
    form.addEventListener("submit", event => {
        if (!window.confirm(form.dataset.confirm)) {
            event.preventDefault();
        }
    });
});
