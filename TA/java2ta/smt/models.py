from contracts import contract
import subprocess
import fcntl
import os
import logging

log_smt = logging.getLogger("smt")

class SMTSolver(object):

    _engine = None

    @staticmethod
    def init_engine(*args, **kwargs):
        SMTProb._engine = SMTProb(*args, **kwargs)

    @staticmethod
    def the_engine():
        return SMTProb._engine

    def __init__(self):
        self._line_num = 0

        self._cmd = subprocess.Popen(["z3", "-in"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)

        # set stderr as non-blocking
        flags = fcntl.fcntl(self._cmd.stderr, fcntl.F_GETFL) # get current p.stdout flags
        fcntl.fcntl(self._cmd.stderr, fcntl.F_SETFL, flags | os.O_NONBLOCK)

    def __del__(self):
        cmd = getattr(self, "_cmd", None)
        if cmd:
            cmd.terminate()


    def __enter__(self):
        """
        interface for calling instance using "with", e.g.

        with SMTProb(...) as prob:  
            ... do something ...
        """
        return self

    
    def __exit__(self, *args, **kwargs):
        """
        interface for calling instance using "with" (see __enter__)
        """
        self._cmd.terminate()

    def _get_error(self):
        err_msg = ""

        try:
            err_msg = " ".join(self._cmd.stderr.readlines())
            self._log_smt(err_msg, is_input=False)
        except IOError:
            # there was no error to read
            pass

        return err_msg

    def _get_output(self):
        out = self._cmd.stdout.readline()
        self._log_smt(out, is_input=False)
        return out

    @contract(commands="list(string)|string", is_input="bool")
    def _log_smt(self, commands, is_input=True):

        line_format = "**<< {line}"
        if is_input:
            line_format = "{line_num}>> {line}"

        if isinstance(commands, basestring):
            commands = commands.split("\n")

        for curr_command in commands:
            if curr_command:
                if is_input:
                    self._line_num = self._line_num + 1
                line = line_format.format(line=curr_command,line_num=self._line_num)
                log_smt.debug(line)


    def _check_error(self, line, default=None):
        
        if line.startswith("(error "):
            raise ValueError(line)
        elif default:
            raise ValueError(default)
    
    @contract(commands="list(string)", returns="string")
    def get_tool_answer(self, commands):
        """
        Receives a list of commands to be executed by the tool.
        Returns the text output of the tool.
        """ 

        self._log_smt(commands)

        commands = "\n".join(commands)
        self._cmd.stdin.write(commands + "\n")

        answer = self._get_output().strip()

        err_msg = self._get_error().strip()
        if err_msg:
            sys.stderr.write(err_msg)

        return answer

    @contract(commands="list(string)", returns="string")
    def check_sat(self, commands):
        """
        Given a list of SMT commands, append the (check-sat) command at the end, and 
        return the tool answer.
        """
        commands.append("(check-sat)")
        return self.get_tool_answer(commands)

    def push(self):
        cmd = "(push)\n"
        self._log_smt(cmd)
        self._cmd.stdin.write(cmd)

        
    def pop(self):
        cmd = "(pop)\n"
        self._log_smt(cmd)
        self._cmd.stdin.write(cmd)


    def get_model(self):
        # TODO run the (get-model) command
        pass


    def get_unsat_core(self):
        # TODO run the (get-unsat-core) command
        pass


