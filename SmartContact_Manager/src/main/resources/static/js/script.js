console.log("this is js page");

const toggleSideBar = () => {
  if ($(".sidebar").is(":visible")) {
    //close sidebar
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
    $(".content").css("width", "100%");
  } else {
    //open sidebar
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "23%");
  }
};

function deleteContact(cId) {
  swal({
    title: "Are you sure?",
    text: "Once deleted, you will not be able to recover this contact !!!",
    icon: "warning",
    buttons: true,
    dangerMode: true,
  }).then((willDelete) => {
    if (willDelete) {
      window.location = "/user/delete/" + cId;
    } else {
      swal("Your contact is safe !!!");
    }
  });
}

const search = () => {
  //console.log("searching contacts...");

  let query = $("#search-input").val();
  if (query == "") {
    $(".search-result").hide();
  } else {
    console.log(query);

    let url = `http://localhost:8282/search/${query}`;

    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        console.log(data);

        let text = `<div class='list-group'>`;

        data.forEach((contact) => {
          text += `<a href='/user/${contact.cid}/contact' class='list-group-item list-group-item-action'> ${contact.name}</a>`;
        });
        text += `</div>`;

        $(".search-result").html(text);
        $(".search-result").show();
      });
  }
};
