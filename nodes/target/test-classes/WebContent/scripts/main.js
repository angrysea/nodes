var myHeading = document.querySelector('h1');
var myButton = document.querySelector('button');
var myImage = document.querySelector('img');
var orgHeading = myHeading.innerHTML;

myImage.onclick = function() {
    var mySrc = myImage.getAttribute('src');
    if(mySrc === 'images/firefox-icon.png') {
      myImage.setAttribute ('src','images/firefox2.jpg');
    } else {
      myImage.setAttribute ('src','images/firefox-icon.png');
    }
}

function onLoad() {
	localStorage.setItem('orgHeading', orgHeading);
	if(!localStorage.getItem('name')) {
	  setUserName();
	} else {
	  var storedName = localStorage.getItem('name');
	  var heading  = localStorage.getItem('orgHeading');
	  myHeading.innerHTML = heading + ', ' + storedName;
	}
}

function setUserName() {
  var myName = prompt('Please enter your name.');
  localStorage.setItem('name', myName);
  myHeading.innerHTML = 'Mozilla is cool, ' + myName;
}

