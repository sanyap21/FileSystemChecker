# FileSystemChecker
This is an Operating System Assignment.

As with any filesystem, there exists the possibility that errors will be introduced.  In Linux, these errors are resolved using a File System ChecKer (fsck).  Each fsck is custom designed for the file system type so that it can examine everything to make sure it is consistent.  

Problem Statement:
 Design a file system checker for our file system. It will have to do the following:
1)	The DeviceID is correct
2)	All times are in the past, nothing in the future
3)	Validate that the free block list is accurate this includes
a.	Making sure the free block list contains ALL of the free blocks
b.	Make sure than there are no files/directories stored on items listed in the free block list
4)	Each directory contains . and .. and their block numbers are correct
5)	Each directory’s link count matches the number of links in the filename_to_inode_dict
6)	If the data contained in a location pointer is an array, that indirect is one
7)	That the size is valid for the number of block pointers in the location array. The three possibilities are:
a.	size<blocksize if  indirect=0 and size>0
b.	size<blocksize*length of location array if indirect!=0
c.	size>blocksize*(length of location array-1) if indirect !=0

