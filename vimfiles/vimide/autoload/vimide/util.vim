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

" ----------------------------------------------------------------------------
"
" Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Makes the supplied path to a legal format, specify to cygwin.
"
" LegalPath:
"   path  - the specific path to make legal, specify to cygwin.
"   ...
"     |- 0 auto mode.
"     |- 1 windows to cygwin
"     \- 2 cygwin to windows
" ----------------------------------------------------------------------------
function! vimide#util#LegalPath(path, ...)
  let path = a:path

  if has('win32unix') 
    if a:0 == 0 || a:000[0] == 0 || a:000[0] == 2
      if a:path =~ '^/cygdrive' " cgywin to windows.
        let path = substitute(a:path, '^/cygdrive/', '', '')
        let path = substitute(path, '^\(\w\)/', '\1:/', '')
      endif
    endif

    if a:0 == 0 || a:000[0] == 0 || a:000[0] == 1
      if a:path =~ '^\w\:' " windows to cygwin.
        let path = substitute(a:path, '^\(\w\)\:', '/cygdrive/\1', '')
        let path = substitute(path, '\', '/', 'g')
      endif
    endif
  endif

  return path
endfunction

" ----------------------------------------------------------------------------
" Execute a command without the specific autocommands.
"
" ExecWithoutAutocmds:
"   cmd - the command to execute.
"   ... - the events to disable.
" ----------------------------------------------------------------------------
function! vimide#util#ExecWithoutAutocmds(cmd, ...)
  let save_opt = &eventignore
  " disabled the supplied autocommands first.
  let events = len(a:000) == 0 ? 'all' : a:000[0]
  exec 'set eventignore=' . events
  try
    exec a:cmd
  finally
    let &eventignore = save_opt
  endtry
endfunction

" ----------------------------------------------------------------------------
" Pad the supplied string.
"
" Pad:
"   string  - the supplied string.
"   length  - the length to format.
"   ...     - the character to pad.
" ----------------------------------------------------------------------------
function! vimide#util#Pad(string, length, ...)
  let char = a:0 > 0 ? a:0 : ' '
  let string = a:string
  while strlen(string) < a:length
    let string .= char
  endwhile
  return string
endfunction

" ----------------------------------------------------------------------------
" Pad the specific table to the same string length every col.
"
" PadTable:
"   table - the specific table to pad.
" ----------------------------------------------------------------------------
function! vimide#util#PadTable(table)
  let table = a:table
  if type(table) == g:LIST_TYPE
    " Determines the max length for every col.
    let nums = []
    for row in table
      if type(row) == g:LIST_TYPE
        let i = 0
        for col in row
          if len(nums) == i
            silent! call add(nums, 0)
          endif

          let l = strlen(col)
          let nums[i] = l > nums[i] ? l : nums[i]
          let i = i + 1
        endfor
      elseif type(row) == g:STRING_TYPE
        if len(nums) == i
          silent! call add(nums, 0)
        endif

        let l = strlen(row)
        let nums[0] = l > nums[0] ? l : nums[0]
      endif
    endfor

    " Performs the pad action to every col.
    for row in table
      if type(row) == g:LIST_TYPE
        let i = 0
        for col in row
          let row[i] = vimide#util#Pad(col, nums[i])
          let i = i + 1
        endfor
      elseif type(row) == g:STRING_TYPE
        let row = vimide#util#Pad(row, nums[0])
      endif
    endfor

    return table
  else
    return []
  endif
endfunction

" ----------------------------------------------------------------------------
" Parses the supplied argument line into a list of args.
"
" ToList:
"   args  - the supplied argument line
" ----------------------------------------------------------------------------
function! vimide#util#ToList(args)
  let args = split(a:args, '[^\\]\s\zs')
  call map(args, 'substitute(v:val, "\\([^\\\\]\\)\\s\\+$", "\\1", "")')
  return args
endfunction

" ----------------------------------------------------------------------------
" Gets the byte offset for the current cursor position or supplied line, col.
"
" GetOffset:
"   line(optional)  - the line number
"   col(optional)   - the col number
" ----------------------------------------------------------------------------
function! vimide#util#GetOffset(...)
  let lnum = a:0 > 0 ? a:000[0] : line('.')
  let cnum = a:0 > 1 ? a:000[1] : col('.')
  let offset = 0

  " handle case where display encoding differs from the underlying file
  " encoding.
  if &fileencoding != '' && &encoding != '' && &fileencoding != &encoding
    let prev = lnum - 1
    if prev > 0
      let lineEncoding = &ff == 'dos' ? "\r\n" : "\n"
      " convert each line to the file encoding and sum their lengths
      let offset = eval(
            \ join(
            \ map(
            \ range(1, prev),
            \ 'len(iconv(getline(v:val), &encoding, &fenc) . "' . lineEncoding . '")'),
            \ '+'))
    endif
  else " normal case
    let offset = line2byte(lnum) - 1
  endif

  let offset += cnum - 1
  return offset
endfunction

" ----------------------------------------------------------------------------
" Reload the current file using ":edit" and perform other operations based on
" the options supplied.
"
" Reload:
"   options
"     |-  retab: Issue a retab of the file taking care of preserving
"     &expandtab before executing the edit to keep indent detection plugins
"     from alwasys setting it to 0 if eclipse inserts some tabbed code that
"     the indent detection plugin uses for its calculations.
"     \-  pos: A line/column pair indicating the new cursor position post
"     edit. When this pair is supplied, this function will attempt to preserve
"     the current window's viewport.
" ----------------------------------------------------------------------------
function! vimide#util#Reload(options)
  let winview = winsaveview()
  let save_expandtabe = &expandtab

  edit!

  if has_key(a:options, 'pos') && len(a:options.pos) == 2
    let lnum = a:options.pos[0]
    let cnum = a:options.pos[1]
    if winheight(0) < line('$')
      let winview.topline += lnum - winview.lnum
      let winview.lnum = lnum
      let winview.col = cnum - 1 
      call winrestview(winview)
    else
      call cursor(lnum, cnum)
    endif
  endif

  if has_key(a:options, 'retab') && a:options.retab
    let &expandtab = save_expandtabe
    retab
  endif
endfunction

" ----------------------------------------------------------------------------
" Parses the supplied list of location entry lines (%f|%l col %c|%m) into a
" vim compatable list of dictionaries that can be passed to setqflist() or
" setloclist().
" In addition to the above line format, this function also supports %f|%l col
" %c|%m|%s, where %s is the type of the entry. The value will be placed in the
" dictionary under the 'type' key.
" The optional 'sort' parameter currently only supports 'severity' as an
" argument.
"
" AssembleLocationEntries:
"   entries         - the specified entries to parse.
"   sort(optional)  - the optional parameter supports for 'severity'.
" ----------------------------------------------------------------------------
function! vimide#util#AssembleLocationEntries(entries, ...)
  if len(a:000) > 0 && a:1 == 'severity'
    let entries = {}
  else
    let entries = []
  endif

  for entry in a:entries
    let dict = s:ParseLocationEntry(entry)

  endfor
endfunction

" vim:ft=vim
