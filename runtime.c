#include <stdio.h>
#include <stdlib.h>
void print (int n) {
   printf ("%d\n", n);
   fflush(stdout);
   /* flush?  A good idea for debugging */
}

int alloc_object(int m){
  int *mn = malloc(m);
  return (int)mn;
}

int alloc_array(int bytes, int quant){
    bytes = bytes + 1;
  int *mn = calloc(bytes,quant);
  return (int)mn;
}
