" Vimide Script.
" Author: keyhom (keyhom.c@gmail.com)
" License: Copyright (c) 2012 keyhom.c@gmail.com.
" 
"   This software is provided 'as-is', without any express or implied warranty.
"   In no event will the authors be held liable for any damages arising from
"   the use of this software.
"   
"   Permission is granted to anyone to use this software for any purpose
"   excluding commercial applications, and to alter it and redistribute it
"   freely, subject to the following restrictions:
"   
"     1. The origin of this software must not be misrepresented; you must not
"     claim that you wrote the original software. If you use this software
"     in a product, an acknowledgment in the product documentation would be
"     appreciated but is not required.
"   
"     2. Altered source versions must be plainly marked as such, and must not
"     be misrepresented as being the original software.
"   
"     3. This notice may not be removed or altered from any source
"     distribution.
"

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:keywords = '\(abstract\|assert\|boolean\|case\|catch\|char\|class\|do\|double\|enum\|extends\|final\|finally\|float\|for\|if\|implements\|import\|int\|interface\|long\|new\|null\|package\|private\|protected\|public\|return\|short\|static\|switch\|throw\|throws\|try\|void\|while\)'

let s:class_declaration = '^\s*\(public\|private\|protected\)\?\(\s\+abstract\)\?\s\+\(class\|interface\|enum\)\s\+[A-Z]'
let s:import_pattern = '^\s*import\_s\+<import>\_s*;'

" ----------------------------------------------------------------------------
"
" Auto Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Determines if the supplied word is valid identifier.
"
" IsValidIdentifier:
"   word  - the word to determine.
" ----------------------------------------------------------------------------
function! vimide#java#util#IsValidIdentifier(word)
  if a:word == '' || a:word =~ '\W' || a:word =~ '^\d\+$' || vimide#java#util#IsKeyword(a:word)
    return 0
  endif
  return 1
endfunction

" ----------------------------------------------------------------------------
" Determines if the supplied word is keyword.
"
" IsKeyword:
"   word  - the word to determine.
" ----------------------------------------------------------------------------
function! vimide#java#util#IsKeyword(word)
  return (a:word =~ '^' . s:keywords . '$\C')
endfunction

" ----------------------------------------------------------------------------
" Gets a array of fields selected in the range.
"
" GetSelectedFields:
"   first - the first line number.
"   last  - the last line number.
" ----------------------------------------------------------------------------
function! vimide#java#util#GetSelectedFields(first, last) range
  " normalize each field statement into a single line.
  let selection = ''
  let index = a:first
  let blockcomment = 0
  while index <= a:last
    let line = getline(index)

    " ignore comment lines.
    if line =~ '^\s*/\*'
      let blockcomment = 1
    endif

    if blockcomment && line =~ '\*/\s*$'
      let blockcomment = 0
    endif

    if line !~ '^\s*//' && !blockcomment
      " remove quoted values.
      let line = substitute(line, '".\{-}"', '', 'g')
      " strip off trailing comments.
      let line = substitute(line, '//.*', '', '')
      let line = substitute(line, '/\*.*\*/', '', '')

      let selection = selection . line
    endif

    let index += 1
  endwhile

  " compact comma separated multi field declarations.
  let selection = substitute(selection, ',\s*', ',', 'g')
  
  " break fields back up into their own line.
  let selection = substitute(selection, ';', ';\n', 'g')

  " remove the assignment portion of the field.
  let selection = substitute(selection, '\(.\{-}\)\s*=.\{-};', '\1;', 'g')

  " extract field names.
  let properties = []
  let lines = split(selection, '\n')
  for line in lines
    if line !~ '^\s*\/\/'
      let fields = substitute(line, '.*\s\(.*\);', '\1', '')
      if fields =~ '^[a-zA-Z0-9_,]'
        for field in split(fields, ',')
          call add(properties, field)
        endfor
      endif
    endif
  endfor

  return properties
endfunction

" vim:ft=vim
