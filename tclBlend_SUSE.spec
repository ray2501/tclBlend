%{!?directory:%define directory /usr}

%define buildroot %{_tmppath}/%{name}

Summary:	Tcl Blend - Java access for Tcl system
Name:		tclBlend
Version:	1.4.8
Release:	0
License:	BSD
Group:		Development/Languages/Tcl
Source0:	%{name}-%{version}.tar.gz
URL:		https://github.com/ray2501/tclBlend
BuildRequires:	autoconf >= 2.50
BuildRequires:	gcc
%if 0%{?suse_version} >= 1500
BuildRequires:	java-11-openjdk-devel
%else
BuildRequires:	java-1_8_0-openjdk-devel
%endif
BuildRequires:	jpackage-utils
BuildRequires:	sed >= 4.0
BuildRequires:	tcl-devel >= 8.6
%if 0%{?suse_version} >= 1500
Requires:       java-11-openjdk-devel
%endif
Requires:       tcl
BuildRoot:	%{buildroot}

%description
Tcl Blend provides two new capabilities to the Tcl system. First, Tcl
Blend provides Java classes that expose key pieces of the interfaces
that are available to extension writers who currently use C. Using
these classes, extension writers can create new commands for the Tcl
interpreter. In addition, Tcl Blend provides commands that allow
script writers to directly manipulate Java objects without having to
write any Java code. The reflection classes in Java make it possible
to invoke methods and access fields on arbitrary objects. Tcl Blend
takes advantage of these capabilities to provide a dynamic interface
to Java.

%prep
%setup -q -n %{name}-%{version}

sed -i -e 's,TCLSH_LOC=\$TCL_BIN_DIR/tclsh,TCLSH_LOC=/usr/bin/tclsh,' tcljava.m4

%build
unset CLASSPATH || :
autoconf
./configure \
        --prefix=%{directory} --libdir=%{directory}/%{_lib}/tcl \
	--with-jdk="%{java_home}" \
	--with-tcl=%{directory}/%{_lib} \
	--with-thread=$(echo %{directory}/%{_lib}/tcl/thread2.*)
make

%install
make install \
	TCLSH=/usr/bin/tclsh \
	prefix=$RPM_BUILD_ROOT%{_prefix} \
        libdir=$RPM_BUILD_ROOT%{_prefix}/%{_lib}/tcl \
	BIN_INSTALL_DIR=$RPM_BUILD_ROOT%{_bindir} \
	LIB_INSTALL_DIR=$RPM_BUILD_ROOT%{_prefix}/%{_lib}/tcl \
	XP_LIB_INSTALL_DIR=$RPM_BUILD_ROOT%{_javadir} \
	TCLBLEND_LIBRARY=$RPM_BUILD_ROOT%{_prefix}/%{_lib}/tcl/tclblend \
	TCLJAVA_INSTALL_DIR=$RPM_BUILD_ROOT%{_prefix}/%{_lib}/tcl/tcljava%{version} \
	XP_TCLJAVA_INSTALL_DIR=$RPM_BUILD_ROOT%{_prefix}/%{_lib}/tcl/tcljava%{version}

%clean
rm -rf %buildroot

%files
%defattr(644,root,root,755)
%doc README changes.txt diffs.txt known_issues.txt license.*
%attr(755,root,root) %{_bindir}/jtclsh
%attr(755,root,root) %{_bindir}/jwish
%dir %{_prefix}/%{_lib}/tcl/tcljava%{version}
%attr(755,root,root) %{_prefix}/%{_lib}/tcl/tcljava%{version}/libtclblend.so
%dir %{_prefix}/%{_lib}/tcl/tcljava%{version}
%{_prefix}/%{_lib}/tcl/tcljava%{version}/pkgIndex.tcl
%{_prefix}/%{_lib}/tcl/tcljava%{version}/tclblend.jar
%{_prefix}/%{_lib}/tcl/tcljava%{version}/tclblendsrc.jar
%{_prefix}/%{_lib}/tcl/tcljava%{version}/tcljava.jar
%{_prefix}/%{_lib}/tcl/tcljava%{version}/tcljavasrc.jar
%{_prefix}/%{_lib}/tcl/xputils
