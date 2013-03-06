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
if !exists('g:VIdeOpenUrlInVimPatterns')
  let g:VIdeOpenUrlInVimPatterns = []
endif

if !exists('g:VIdeOpenUrlInVimAction')
  let g:VIdeOpenUrlInVimAction = g:VIdeDefaultFileOpenAction
endif

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------
let s:win_browsers = [
      \ 'C:/Program Files/Mozilla Firefox/firefox.exe',
      \ 'C:/Program Files/Internet Explorer/iexplore.exe',
      \ 'C:/Program Files/Opera/Opera.exe',
      \ ]

let s:browsers = [
      \ 'firefox', 'chromium-browser', 'iexplore', 'xdg-open', 'opera', 'konqueror', 'epiphany', 'mozilla', 
      \ 'netscape',
      \ ]

" ----------------------------------------------------------------------------
"
" Script Functions:
"
" ----------------------------------------------------------------------------
function! s:DetermineBrowser()
  let browser = ''

  " user specified a browser, we just need to fill in any gaps if necessary.
  if exists('g:VIdeBrowser')
    let browser = g:VIdeBrowser
    " add "<url>" if necessary
    if browser !~ '<url>'
      let browser = substitute(browser, 
            \ '^\([[:alnum:][:blank:]-/\\_.:"]\+\)\(.*\)$',
            \ '\1 "<url>" \2', '')
    endif

    if has('win32') || has('win64')
      " add 'start' to run process in background if necessary.
      if browser !~ '^[!]\?start'
        let browser = 'start ' . browser
      endif
    else
      " add '&' to run process in background if necessary.
      if browser !~ '&\s*$' && 
            \ browser !~ '^\(/[/a-zA-Z0-9]\+/\)\?\<\(links\|lynx\|elinks\|w3m\)\>'
        let browser = browser . ' &'
      endif

      " add redirect of std out and error if necessary.
      if browser !~ '/dev/null'
        let browser = substitute(browser, '\s*&\s*$', '&>/dev/null &', '')
      endif
    endif

    if browser !~ '^\s*!'
      let browser = '!' . browser
    endif
  else
    " user did not specify a browser, so attampt to find a suitable one.
    if has('win32') || has('win64') || has('win32unix')
      " Note: this version may not like .html suffixes on windows 2000.
      if executable('rundll32')
        let browser = rundll32 url.dll,FileProtocolHandler <url>'
      endif

      " this doesn't handle local files very well or '&' in the url.
      " let browser = '!cmd /c start <url>'
      if browser == ''
        for name in s:win_browsers
          if has('win32unix')
            let name = vimide#util#LegalPath(name, 1)
          endif

          if executable(name)
            let browser = name
            if has('win32')
              let browser = '"' . browser . '"'
            endif
            break
          endif
        endfor
      endif
    elseif has('mac')
      let browser = '!open <url>'
    else
      for name in s:browsers
        if executable(name)
          let browser = name
          break
        endif
      endfor
    endif

    if browser != ''
      let g:VIdeBrowser = browser
      let browser = s:DetermineBrowser()
    endif
  endif

  if browser == ''
    call vimide#print#EchoError('Unable to determine browser.  ' . 
          \ 'Please set g:VIdeBrowser to your preferred browser.')
  endif

  return browser
endfunction

" ----------------------------------------------------------------------------
"
" Autocmd Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Opens the supplied url in browser.
"
" OpenUrl:
"   url       - the url which to transfer.
"   [options] - the options.
" ----------------------------------------------------------------------------
function! vimide#web#OpenUrl(url, ...)
  " opens the supplied url in a web browser or opens the url under the cursor.

  if !exists('s:browser') || s:browser == ''
    let s:browser = s:DetermineBrowser()

    " slight hack for IE which doesn't like the url to be quoted.
    if s:browser =~ 'iexplorer' && !has('win32unix')
      let s:browser = substitute(s:browser, '"', '', 'g')
    endif
  endif

  if s:browser == ''
    return
  endif

  let url = a:url
  if url == ''
    if len(a:000) > 2
      let start = a:000[1]
      let end = a:000[2]
      while start <= end
        call vimide#web#OpenUrl(vimide#util#GrabUri(start, col('.')), a:000[0])
        let start += 1
      endwhile
      return
    else
      let url = vimide#util#GrabUri()
    endif
  endif

  if url == ''
    call vimide#print#EchoError(
          \ 'No url supplied at command line or found under the cursor.')
    return
  endif

  " prepend http:// or file:// if no protocol defined.
  if url !~ '^\(https\?\|file\):'
    " absolute file on windows or unix
    if url =~ '^\([a-zA-Z]:[/\\]\|/\)'
      let url = 'file://' . url
    else
      " everything else.
      let url = 'http://' . url
    endif
  endif

  if len(a:000) == 0 || a:000[0] == ''
    for pattern in g:VIdeOpenUrlInVimPatterns
      if url =~ pattern
        exec g:VIdeOpenUrlInVimAction . ' ' . url
        return
      endif
    endfor
  endif

  let url = substitute(url, '\', '/', 'g')
  let url = escape(url, '&%!')
  let url = escape(url, '%!')
  let command = escape(substitute(s:browser, '<url>', url, ''), '#')
  " echo command
  " return
  silent exec command
  " redraw!

  if v:shell_error
    call vimide#print#EchoError("Unable to open browser:\n" . s:browser . 
          \ "\nCheck that the browser executable is in your PATH " . 
          \ 'or that you have property configured g:VIdeBrowser')
  endif
endfunction

" ----------------------------------------------------------------------------
" Determines the browsers or select by the supplied browser.
"
" ChooseBrowser:
"   [browser] -  the browser to set.
" ----------------------------------------------------------------------------
function! vimide#web#ChooseBrowser(...)
  let browser = ''
  if a:0 > 0
    let browser = a:1
  else
    let browsers = []
    if has('win32') || has('win64')
      let browsers = s:win_browsers
    else
      let browsers = s:browsers
    endif
    let index = vimide#util#PromptList("Pleases choose the browser below, CTRL+C to cancel.\n", browsers)
    if 0 <= index
      let browser = browsers[index]
    endif
  endif

  if browser != ''
    let g:VIdeBrowser = browser
    let s:browser = s:DetermineBrowser()
    call vimide#print#Echo("Set the browser to '" . browser . "'")
  endif
endfunction

" vim:ft=vim
