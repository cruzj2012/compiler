/*
    main.s -- a simple program for the GNU SPARC assembler, "as".

    This program should be made executable as follows:
        sparc-linux-as main.s -o main.o
        sparc-linux-ld -dynamic-linker=/lib/ld-uClibc.so.0 -e start main.o -o main
*/
	/* define SYS_exit = 1 (from sys/syscalls.h)  */
	.set   SYS_exit, 1

        /* exit -- trap to operating system */
	.macro exit_program
        clr 	%o0		! %o0 := 0;  program status=0=success
	mov	SYS_exit, %g1	! %g1 := SYS_exit; determine system call
	ta	0x90
	.endm
