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

let s:command_constructor = 
      \ '/javaConstructor?project=<project>&file=<file>&offset=<offset>'

let s:command_fields = 
      \ '/javaBeanProperties?project=<project>&file=<file>&offset=<offset>&type=<type>'

let s:command_impl = '/javaImpl?project=<project>&file=<file>&offset=<offset>'
let s:command_impl_insert = '/javaImpl?project=<project>&file=<file>&type=<type>&superType=<superType>'

let s:no_fields = 
      \ 'Unable to find property at current cursor position: '. 
      \ 'Not an a field declaration or possible java syntax error.'
let s:cross_type_selection = 'Visual selection is currently limited to methods of one super type at a time.'

" ----------------------------------------------------------------------------
"
" Scripts Functions:
"
" ----------------------------------------------------------------------------

function! s:AddImpl(visual)
  call vimide#java#impl#Add(s:command_impl_insert, function("vimide#java#impl#ImplWindow"), a:visual)
endfunction

function! s:MethodSig(line)
  let sig = substitute(a:line, '.*\s\(\w\+(.*\)', '\1', '')
  let sig = substitute(sig, ',\s', ',', 'g')
  let sig = substitute(sig, '<.\{-}>', '', 'g')
  return sig
endfunction

" ----------------------------------------------------------------------------
"
" Autocmd Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Generates a constructor by selecting range or the element under the cursor.
"
" GenerateConstructor:
"   first - the first element line number.
"   last  - the last element line number.
"   bang  - the bang.
" ----------------------------------------------------------------------------
function! vimide#java#impl#GenerateConstructor(first, last, bang)
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  call vimide#lang#SilentUpdate()

  let properties = a:last == 1 ? [] :
        \ vimide#java#util#GetSelectedFields(a:first, a:last)

  let project = vimide#project#impl#GetProject()
  let file = expand('%:p')

  let command = s:command_constructor
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')
  let command = substitute(command, '<offset>', vimide#util#GetOffset(), '')

  if a:bang == ''
    let command .= '&super=1'
  endif

  if len(properties) > 0
    for prop in properties
      if '' != prop
        let command .= '&field=' . prop
      endif
    endfor
  endif

  let result = vimide#Execute(command)

  if type(result) == g:STRING_TYPE && result != ''
    call vimide#print#EchoError(result)
    return
  endif

  if result != '0'
    call vimide#util#Reload({'retab' : 1})
    write
  endif
endfunction

" ----------------------------------------------------------------------------
" Generates the getter/setter for the selected fields.
"
" GenerateGetterSetter:
"   first - the first element line number.
"   last  - the last element line number.
"   bang  - the bang.
"   type  - the type which generate to.
"     0: both getter and setter.
"     1: just getter.
"     2: just setter.
" ----------------------------------------------------------------------------
function! vimide#java#impl#GenerateGetterSetter(first, last, bang, type)
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  call vimide#lang#SilentUpdate()

  let fields = vimide#java#util#GetSelectedFields(a:first, a:last)

  if len(fields) == 0
    call vimide#print#EchoError(s:no_fields)
    return
  endif

  let project = vimide#project#impl#GetProject()
  let file = expand('%:p')
  let indexed = a:bang != '' ? 1 : 0

  let command = s:command_fields
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')
  let command = substitute(command, '<offset>', vimide#util#GetOffset(), '')
  let command = substitute(command, '<type>', a:type, '')

  for f in fields
    let command .= '&field=' . f
  endfor

  if indexed
    let command .= '&indexed=1'
  endif

  let result = vimide#Execute(command)
  if result != '0'
    call vimide#util#Reload({'retab' : 1})
    write
  endif
endfunction

" ----------------------------------------------------------------------------
" Makes a implementation/overriden request for the specific type.
"
" Impl:
" ----------------------------------------------------------------------------
function! vimide#java#impl#Impl()
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  call vimide#lang#SilentUpdate()

  let project = vimide#project#impl#GetProject()
  let file = expand('%:p')
  let offset = vimide#util#GetCurrentElementOffset()

  let command = s:command_impl
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')
  let command = substitute(command, '<offset>', offset, '')

  call vimide#java#impl#ImplWindow(command)
endfunction

" ----------------------------------------------------------------------------
" ImplWindow:
" ----------------------------------------------------------------------------
function! vimide#java#impl#ImplWindow(command)
  if (vimide#java#impl#Window(a:command, "impl")) 
    nnoremap <silent> <buffer> <cr> :call <SID>AddImpl(0)<cr>
    vnoremap <silent> <buffer> <cr> :<C-U>call <SID>AddImpl(1)<cr>
  endif
endfunction

" ----------------------------------------------------------------------------
"  ImplWindowFolding:
" ----------------------------------------------------------------------------
function! vimide#java#impl#ImplWindowFolding()
  setlocal foldmethod=syntax
  setlocal foldlevel=99
endfunction

" ----------------------------------------------------------------------------
" Add:
" ----------------------------------------------------------------------------
function! vimide#java#impl#Add(command, function, visual)
  let winnr = bufwinnr('^' . b:filename)
  " src window is not longer open.
  if winnr == -1
    call vimide#print#EchoError(b:filename . " no longer found in the open windows.")
    return
  endif

  if a:visual
    let start = line("'<")
    let end = line("'>")
  endif

  let superType = ""
  let methods = []
  " non-visual mode or only are line selected.
  if !a:visual || start == end
    " not a valid selection
    if line('.') == 1 || getline('.') =~ '^\(\s*//\|package\|$\|}\)'
      return
    endif

    let line = getline('.')
    if line =~ '^\s*throws'
      let line = getline(line('.') - 1)
    endif

    " on a method line.
    if line =~ '^\s\+'
      call add(methods, s:MethodSig(line))
      let ln = search('^\w', 'bWn')
      if ln > 0
        let superType = substitute(getline(ln), '.*\s\+\(.*\) {', '\1', '')
      endif
    else
      " on a type line.
      let superType = substitute(line, '.*\s\+\(.*\) {', '\1', '')
    endif
  else
    " visual mode
    let pos = getpos('.')
    let index = start
    while index <= end
      let line = getline(index)
      if line =~ '^\s*\($\|throws\|package\)'
        " do nothing.
      elseif line =~ '^\s\+'
        " on a method line.
        call add(methods, s:MethodSig(line))
        call cursor(index, 1)
        let ln = search('^\w', 'bWn')
        if ln > 0
          let super = substitute(getline(ln), '.*\s\(.*\) {', '\1', '')
          if superType != '' && super != superType
            call vimide#print#EchoError(s:cross_type_selection)
            call setpos('.', pos)
            return
          endif
          let superType = super
        endif
      else
        " on a type line.
        let super = substitute(line, '.*\s\(.*\) {', '\1', '')
        if superType != '' && super != superType
          call vimide#print#EchoError(s:cross_type_selection)
          call setpos('.', pos)
          return
        endif
        let superType = super
      endif
      call setpos('.', pos)

      let index += 1
    endwhile

    if superType == ''
      return
    endif
  endif

  " search up for the nearest package.
  let ln = search('^package', 'bWn')
  if ln > 0
    let package = substitute(getline(ln), '.*\s\(.*\);', '\1', '')
    let superType = package . '.' . substitute(superType, '<.\{-}>', '', 'g')
  endif

  let type = substitute(getline(1), '\$', '.', 'g')
  let impl_winnr = winnr()
  exec winnr . 'winc w'
  call vimide#lang#SilentUpdate()

  let project = vimide#project#impl#GetProject()
  let file = expand('%:p')

  let command = a:command
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')
  let command = substitute(command, '<type>', type, '')
  let command = substitute(command, '<superType>', superType, '')

  if len(methods)
    for m in methods
      let command .= '&method=' . m
    endfor
  endif

  call a:function(command)

  noautocmd exec winnr . 'winc w'
  call vimide#util#Reload({'retab' : 1})
  write
  noautocmd exec impl_winnr . 'winc w'
endfunction

" ----------------------------------------------------------------------------
" Window:
" ----------------------------------------------------------------------------
function! vimide#java#impl#Window(command, name)
  let name = expand('%:p') . '_' . a:name
  let project = vimide#project#impl#GetProject()

  let result = vimide#Execute(a:command)
  if type(result) == g:STRING_TYPE
    call vimide#print#EchoError(result)
    return
  endif

  if type(result) != g:DICT_TYPE
    return
  endif

  let content = [result.type]
  for super in result.superTypes
    call add(content, '')
    call add(content, 'package ' . super.packageName  . ';')
    call add(content, super.signature . ' {')
    for method in super.methods
      let signature = split(method, '\n')
      let content += map(signature, '"\t" . v:val')
    endfor
    call add(content, '}')
  endfor

  call vimide#window#TempWindow(name, content, {'preserveCursor': 1})
  setlocal ft=java
  call vimide#java#impl#ImplWindowFolding()
  return 1
endfunction

" vim:ft=vim
