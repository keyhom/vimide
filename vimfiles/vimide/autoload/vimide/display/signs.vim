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
" Global Variables:
"
" ----------------------------------------------------------------------------

if !exists('g:VIdeShowQuickfixSigns')
  let g:VIdeShowQuickfixSigns = 1
endif

if !exists('g:VIdeShowLoclistSigns')
  let g:VIdeShowLoclistSigns = 1
endif

if !exists('g:VIdeQuickfixSignText')
  let g:VIdeQuickfixSignText = '> '
endif

if !exists('g:VIdeLoclistSignText')
  let g:VIdeLoclistSignText = '>>'
endif

if !exists('g:VIdeUserSignText')
  let g:VIdeUserSignText = '#'
endif

if !exists('g:VIdeUserSignHighlight')
  let g:VIdeUserSignHighlight = g:VIdeInfoHighlight
endif


" ----------------------------------------------------------------------------
" Defines a new sign name or updates an existing one.
"
" Define:
"   name      - the name of the sign.
"   text      - the text of the sign.
"   highlight - the highlight of the sign.
" ----------------------------------------------------------------------------
function! vimide#display#signs#Define(name, text, highlight)
  exec 'sign define ' . a:name . ' text=' . a:text . ' texthl=' . a:highlight
endfunction

" ----------------------------------------------------------------------------
" Undefines a sign by name.
"
" Undefine:
"   name  - the specific name ths sign was.
" ----------------------------------------------------------------------------
function! vimide#display#signs#Undefine(name)
  exec 'sign undefine ' . a:name
endfunction

" ----------------------------------------------------------------------------
" Places a sign in the current buffer.
"
" Place:
"   name  - the name of the sign.
"   line  - the line the sign place to.
" ----------------------------------------------------------------------------
function! vimide#display#signs#Place(name, line)
  if a:line > 0
    let lastline = line('$')
    let line = a:line <= lastline ? a:line : lastline
    let id = a:name == 'placeholder' ? 999999 : line
    exec 'sign place ' . id . ' line=' . line . ' name=' . a:name . ' buffer=' . 
          \ bufnr('%')
  endif
endfunction

" ----------------------------------------------------------------------------
" Unplaces a sign in the current buffer.
"
" Unplace:
"   id  - the specific id the sign was.
" ----------------------------------------------------------------------------
function! vimide#display#signs#Unplace(id)
  exec 'sign unplace ' . a:id . ' buffer=' . bufnr('%')
endfunction

" ----------------------------------------------------------------------------
" Places a sign in the current buffer for each line in the list.
" 
" PlaceAll:
"   name  - the name of the sign.
"   list  - the list those need place to.
" ----------------------------------------------------------------------------
function! vimide#display#signs#PlaceAll(name, list)
  let lastline = line('$')
  for line in a:list
    if line > 0
      let line = line <= lastline ? line : lastline
      exec 'sign place ' . line . ' line=' . line . ' name=' . a:name . 
            \ ' buffer=' . bufnr('%')
    endif
  endfor
endfunction

" ----------------------------------------------------------------------------
" Unplaces all signs in the supplied list from the current buffer.
" The list may be a list of ids or a list of dictionaries as returned by
" GetExisting().
"
" UnplaceAll:
"   list  - the specific list.
" ----------------------------------------------------------------------------
function! vimide#display#signs#UnplaceAll(list)
  for sign in a:list
    if type(sign) == g:DICT_TYPE
      call vimide#display#signs#Unplace(sign['id'])
    else
      call vimide#display#signs#Unplace(sign)
    endif
  endfor
endfunction

" ----------------------------------------------------------------------------
" Gets a list of existing signs for the current buffer.
" Optionally a sign name may be supplied to only retrieves signs of that name.
"
" GetExisting:
"   id    - the sign id.
"   line  - the line number.
"   name  - the sign name.
" ----------------------------------------------------------------------------
function! vimide#display#signs#GetExisting(...)
  let bufnr = bufnr('%')

  redir => signs
  silent exec 'sign place buffer=' . bufnr
  redir END

  let existing = []
  for line in split(signs, "\n")
    if line =~ '.\{-}=.\{-}=' " only two equals to account for swedish output.
      call add(existing, s:ParseSign(line))
    endif
  endfor

  if len(a:000) > 0
    call filter(existing, "v:val.name == a:000[0]")
  endif

  return existing
endfunction

" ----------------------------------------------------------------------------
" Determines if there was any existing signs.
" Optionally a sign name may be supplied to only test for signs of that name.
"
" HasExisting:
"   line  - the line number.
" ----------------------------------------------------------------------------
function! vimide#display#signs#HasExisting(...)
  let bufnr = bufnr('%')
  redir => results
  silent exec 'sign place buffer=' . bufnr
  redir END

  for line in split(results, "\n")
    if line =~ '.\{-}=.\{-}=' " only two equals to account for swedish output.
      if len(a:000) == 0
        return 1
      endif
      let sign = s:ParseSign(line)
      if sign.name == a:000[0]
        return 1
      endif
    endif
  endfor

  return 0
endfunction

" ----------------------------------------------------------------------------
" ParseSign: (Script Functions)
" ----------------------------------------------------------------------------
function! s:ParseSign(raw)
  let attrs = split(a:raw)

  exec 'let line=' . split(attrs[0], '=')[1]

  let id = split(attrs[1], '=')[1]
  " hack for the italian localization
  if id =~ ',$'
    let id = id[:-2]
  endif

  " hack for swedish localization
  if attrs[2] =~ '^namn'
    let name = substitute(attrs[2], 'namn', '', '')
  else
    let name = split(attrs[2], '=')[1]
  endif

  return {'id': id, 'line': line, 'name': name}
endfunction

" ----------------------------------------------------------------------------
" Updates the signs for the current buffer. This function will read both the
" location list and the quickfix list and place a sign for any entries for the
" current file.
" This function supports a severity level by examining the 'type' key of the
" dictionaries in the location or quickfix list. It supports 'i' (info), 'w'
" (warning), 'e' (error).
"
" Update:
" ----------------------------------------------------------------------------
function! vimide#display#signs#Update()
  if !has('signs') || !g:VIdeSignLevel
    return
  endif

  let save_lazy = &lazyredraw
  set lazyredraw

  let placeholder = vimide#display#signs#SetPlaceholder()

  let qflist = filter(g:VIdeShowQuickfixSigns ? getqflist() : [], 'bufnr("%") == v:val.bufnr')
  let loclist = filter(g:VIdeShowLoclistSigns ? getloclist(0) : [], 'bufnr("%") == v:val.bufnr')

  " remove all existing signs
  let existing = vimide#display#signs#GetExisting()
  for exists in existing
    if exists.name =~ '^\(qf_\)\?\(error\|info\|warning\)$'
      call vimide#display#signs#Unplace(exists.id)
    endif
  endfor

  for [list, marker, prefix] in [[qflist, g:VIdeQuickfixSignText, 'qf_'],
        \ [loclist, g:VIdeLoclistSignText, '']]
    if g:VIdeSignLevel >= 4
      let info = filter(copy(list), 'v:val.type == "" || tolower(v:val.type) == "i"')
      call vimide#display#signs#Define(prefix . 'info', marker, g:VIdeInfoHighlight)
      call vimide#display#signs#PlaceAll(prefix . 'info', map(info, 'v:val.lnum'))
    endif

    if g:VIdeSignLevel >= 3
      let warnings = filter(copy(list), 'tolower(v:val.type) == "w"')
      call vimide#display#signs#Define(prefix . 'warning', marker, g:VIdeWarningHighlight)
      call vimide#display#signs#PlaceAll(prefix . 'warning', map(warnings, 'v:val.lnum'))
    endif

    if g:VIdeSignLevel >= 2
      let errors = filter(copy(list), 'tolower(v:val.type) == "e"')
      call vimide#display#signs#Define(prefix . 'error', marker, g:VIdeErrorHighlight)
      call vimide#display#signs#PlaceAll(prefix . 'error', map(errors, 'v:val.lnum'))
    endif
  endfor

  if placeholder
    call vimide#display#signs#RemovePlaceholder()
  endif

  let &lazyredraw = save_lazy
endfunction

" ----------------------------------------------------------------------------
" Sets sign at line 1 to prevent sign column from collapsing, and subsiquent
" screen redraw.
" 
" SetPlaceholder:
"   only_if_necessary (optional) - if 1, only set a placeholder if there are no existing
"   signs.
" ----------------------------------------------------------------------------
function! vimide#display#signs#SetPlaceholder(...)
  if !has('signs') || !g:VIdeSignLevel
    return
  endif

  if len(a:000) > 0 && a:000[0]
    let existing = vimide#display#signs#GetExisting()
    if !len(existing)
      return
    endif
  endif

  call vimide#display#signs#Define('placeholder', '><', g:VIdeInfoHighlight)
  let existing = vimide#display#signs#GetExisting('placeholder')
  if len(existing) == 0 && vimide#display#signs#HasExisting()
    call vimide#display#signs#Place('placeholder', 1)
    return 1
  endif
  return
endfunction

" ----------------------------------------------------------------------------
" Removes the specific placeholder.
"
" RemovePlaceholder:
" ----------------------------------------------------------------------------
function! vimide#display#signs#RemovePlaceholder()
  if !has('signs') || !g:VIdeSignLevel
    return
  endif

  let existing = vimide#display#signs#GetExisting('placeholder')
  for exists in existing
    call vimide#display#signs#Unplace(exists.id)
  endfor
endfunction

if has('signs')
  call vimide#display#signs#Define('user', g:VIdeUserSignText, g:VIdeUserSignHighlight)
endif

" vim:ft=vim
