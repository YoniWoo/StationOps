// ouvre et ferme le menu lateral sur telephone
const menu = document.querySelector(".menu");
const boutonMenu = document.querySelector(".bouton-menu");

if (menu && boutonMenu) {
    boutonMenu.addEventListener("click", function () {
        menu.classList.toggle("ouvert");
    });
}
