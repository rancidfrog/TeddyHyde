
console.log( "Inside the asciidoc rendering..." );

document.write( "We have something here<br/>" );

var asciidocMarkup = "http://asciidoctor.org[*Asciidoctor*] " +
                          "running on http://opalrb.org[_Opal_] " +
                          "brings AsciiDoc to the browser!";
var html  = Opal.Asciidoctor.$render( asciidocMarkup );

console.log( "Rendered..." );

document.write( html );

console.log( "Inserted" );