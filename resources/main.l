function 2x3 adder(a,b,c)
  s:=a xor b xor c
  d:=(not s) or a or b or c
  s,d



  0 0 0  0 0
  0 0 1  0 1
  0 1 0  0 1
  0 1 1  1 0
  1 0 0  0 1
  1 0 1  1 0
  1 1 0  1 0
  1 1 1  1 1