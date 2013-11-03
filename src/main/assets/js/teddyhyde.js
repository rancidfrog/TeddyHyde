console.log( "Inside the asciidoc rendering..." );
var html  = Opal.Asciidoctor.$render( asciidocMarkup );
console.log( "Rendered..." );
document.write( html );
console.log( "Inserted" );