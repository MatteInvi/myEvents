

// Sezione per mostrare antreprima delle foto da caricare
document.addEventListener("DOMContentLoaded", function () {
  let fileInput = document.getElementById("fileInput");
  fileInput.addEventListener("change", previewImages);
});

function previewImages(event) {
  let preview = document.getElementById("preview");
  preview.innerHTML = "";
  let files = event.target.files;
  let title = document.createElement("h4");
   title.textContent = "Anteprima: ";
    preview.appendChild(title);

  for (let i = 0; i < files.length; i++) {
    let reader = new FileReader();

    reader.onload = (function () {
      return function (e) {        
        let img = document.createElement("img");   
        img.src = e.target.result;
        img.className = "img-preview"       
        preview.appendChild(img);
      };
    })(files[i]);

    reader.readAsDataURL(files[i]);
  }
}

// Sezione per mostrare le immagini ingrandite al click
document.addEventListener("DOMContentLoaded", function() {
    let imageModal = document.getElementById('imageModal');
    let modalImage = document.getElementById('modalImage');

    imageModal.addEventListener('show.bs.modal', function (event) {
        let trigger = event.relatedTarget;
        let imgUrl = trigger.getAttribute('data-bs-image');
        modalImage.src = imgUrl;
    });
});
