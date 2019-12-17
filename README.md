# CipherCrack
Cipher Crack

<h1>Overview</h1>
Following on from an earlier Perl project this provides an Android app that can encode and decode a number of classic ciphers, perform static analysis with suggested likely ciphers, share results via email and most importantly crack ciphers using a variety of techniques.

Ciphers included range from simple Caesar, Affine and Keyword Substitution, through transposition ciphers such as column permutation, Skytale and Railfence, across Polyalphabetic such as Vigenere and Beaufort, and Polygraphic examples like Playfair, Hill and Polybius Square.

Editing capability includes splitting and reordering text, adjusting case and separating words in a few different languages. Text can be cut and pasted from another app (browser, etc) inside the phone or captured using OCR via the camera on the device.

The project was commenced in 2019 in anticipation of the National Cipher Challenge run annually by Southampton University, and was useful for cracking ciphers on a mobile (Moto G6) when away from home. It also contains examples of previous ciphers going back to 2001, for practice, thanks to the cipher archive provided by themaddoctor at https://github.com/themaddoctor/BritishNationalCipherChallenge, and examples from Simon Singh's Code Book: https://en.wikipedia.org/wiki/The_Code_Book

<h1>Tech Stack</h1>
The Java code is mostly self-contained, and includes basic matrix multiplication and inversion (Hill Cipher), custom-built word splitting and a background service to perform the cracking while other activity can take place.

This project also provided me with a real world example to refresh how Android development is done after a couple of years away from it.

<h1>Future Work</h1>
In no particular order
<ul>
<li>Better Cracking Techniques
<li>More ciphers: Amsco, Bifid, Trithemius, Porta, other substitutions such as numeric and graphic
<li>More languages beyond English, German and Dutch
<li>Use the static analysis to automate the breaking of an unknown cipher by choosing the likely cipher and best method to crack
<li>Improve the accuracy of the camera captures
<li>Better look and feel - icons, colours, links to Wikipedia entries, etc
<li>Allow programmatic creation of pipeline of steps, e.g. reverse words, then apply Vigenere, then do railfence... etc.
</ul>

<h1>Contact</h1>
Contact: martin_hardaker@yahoo.com
