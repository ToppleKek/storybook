# .storybook file format specifications
A simple file format for storing stories used in this application.

## Header - Items in this order
- Null terminated title string
- Null terminated author string
- Number of pages : uint16 (numPages)

## Pages - This structure `numPages` times
- Page of choice 1 : uint16 (choice1) *Can be 0 to note that this is not a valid choice
- Page of choice 2 : unit16 (choice2) *Can be 0 to note that this is not a valid choice
- Length of the compressed image data : uint32 (imageLen) *Can be 0 for no image
- ZLIB compressed base64 image string that is `imageLen` bytes long
- Length of the compressed text data : uint32 (textLen) *Can be 0 for no text
- ZLIB compressed text that is `textLen` bytes long