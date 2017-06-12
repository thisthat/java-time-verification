from java2ta.abstraction.domains import Domain

class AbstractAttribute(object):

    def __init__(self, name, domain, is_local):
        """
        An AbstractAttribute has a name, a domain and is either a local attribute (i.e. declared within the
        analysed code) or is global (i.e. from another component or the environment)
        """

        assert isinstance(name, basestring)
        assert isinstance(domain, Domain)
        assert isinstance(is_local, bool)

        values = domain.values
        initial = []

        self.is_local = is_local

        if is_local:
            initial = [ domain.default ]
        else:
            initial = values            

        self.domain = domain
        self.name = name

#        assert isinstance(values, list), "Expected list. Passed '%s'" % values
        assert hasattr(values, "__iter__"), "Expected iterable. Passed '%s'" % values

        self.enc_values = {}
        self.rev_domain = {}

        # encode the passed values to form an integer domain; at the same
        # time keep track of the mapping between domain values and abstract value
        for (pos,item) in enumerate(values):
            self.enc_values[pos] = item

            if item in self.rev_domain:
                raise Exception("You passed a list of values containing twice '%s'. Duplicates are not allowed." % item)

            self.rev_domain[item] = pos

        for curr in initial:
            assert curr in values, "Expected value in '%s'. Passed: '%s'" % (",".join(values), curr)

        # store the encoded values of the passed initial values
        self.initial = map(lambda val: self.rev_domain[val], initial)

    
    @property
    def encoded_values(self):
        return sorted(self.enc_values.keys())

    def encoded_value(self, value):
        if value not in self.rev_domain:
            raise ValueError("The passed value ('%s') is not one of the allowed ones . Accepted values: %s" % (value, ",".join(self.values)))
    
        return self.rev_domain[value]

    def primed(self):       
        primed_name = "%s_1" % self.name
        new_attr = AbstractAttribute(primed_name, self.domain, self.is_local)
        return new_attr

    @property
    def values(self):
        return sorted(self.rev_domain.keys())

    def value(self, encoding):
        if encoding not in self.enc_values:
            raise ValueError("The passed encoding ('%s') is not one of the allowed ones. Accepted encodings: %s" % (encoding, ",".join(self.encoded_values)))

        return self.enc_values[encoding]



class StateSpace(object):

    def __init__(self):
        self._attributes = {}
        self._configurations = [] #set([])
        self._initial_configurations = [] #set([])

    def add_attribute(self, attribute):
        """
        Add an instance of AbstractAttribute to the state spacae.
        """
        assert isinstance(attribute, AbstractAttribute)
        self._attributes[attribute.name] = attribute
        # invalidate cached configurations
        self._configurations = [] #set([])
        self._initial_configurations = [] #set([])

    def get_attribute(self, name):
        """
        Return the AbstractAttribute with the given name.
        """
        res = self._attributes[name]

        assert isinstance(res, AbstractAttribute)
        return res

    @property
    def attributes(self):
        """
        Return the attributes that make the state space, in alphabetical order.
        """
        sort_by_name = lambda att: att.name

        return sorted(self._attributes.values(), key=sort_by_name)
 
    @property   
    def enumerate(self):
        """
        A configuration is a tuple describing the product of all attribute values. The i-th item of the
        tuple is the value of the i-th attribute. Attributes are considered in the alphabetical order.
        """
        if not self._configurations:
            # enumerate configurations and store for later use
            self._configurations = [()] # start with a single empty configuration
            for attr in self.attributes:
                self._configurations = self._do_multiply(self._configurations, attr)

        return self._configurations

    @property
    def initial_configurations(self):
        """
        Filter the enumerated configurations and return only the initial ones, i.e. where
        all the attributes have the initial value.
        """
        if not self._initial_configurations:
            self._initial_configurations = [] #set([])

            for conf in self.enumerate:
                assert isinstance(conf, tuple)

                # the configuration is considerd initial *if-and-only-if* all the attributes
                # have the initial value
                is_initial = True
                for (attr,val) in zip(self.attributes, conf):
                    assert isinstance(attr, AbstractAttribute)
                    if val not in attr.initial:
                        is_initial = False
                        break

                if is_initial:
                    # cache only initial configurations
                    self._initial_configurations.append(conf)
     
        assert isinstance(self._initial_configurations, list)       
        return self._initial_configurations


    @property
    def size(self):
        """
        Return the number of configurations that constitute the state space. This equals the
        product of the sizes of the domains of the attributes in the state space itself.
        """
        return len(self.enumerate)


    def _do_multiply(self, configurations, attribute):
        """     
        Take as input a tuple describing a partial configuration, and produce a list of tuples
        obtained by combining the passed tuple with each possible value of the domain of the
        passed attribute. 

        By calling this method for each attribute of the state space, starting with the empty 
        tuple, and collecting the configuration tuples produced by the last call, one obtains 
        all the possible configurations of attributes in the state space.
        """

        assert isinstance(configurations, list)
        assert isinstance(attribute, AbstractAttribute)

        new_configurations = []
        for conf in configurations:

            for val in attribute.encoded_values:
                new_tuple = conf + (val,)
                new_configurations.append(new_tuple)

        return new_configurations

    def value(self, configuration):
        """
        Given a particular configuration of values of the state space attributes, explicit the value of 
        each attribute making the configuration.

        Use this method to **decode** the attributes values from a given configuration.
        """

        assert isinstance(configuration, tuple)

        attributes = self.attributes

        if len(configuration) != len(attributes):
            raise Exception("Statespace has %s attributes, thus it can decode configurations with %s elements. Got configuration with %s elements" % (len(attributes), len(attributes), len(configuration)))

        decoded = ()
        for (att,conf_val) in zip(attributes, configuration):
            decoded += (att.value(conf_val), )

        return decoded

    def configuration(self, values):
        """
        Given a tuple of values (the i-th item is interpreted as the value of the i-th attribute),
        produce a configuration, i.e. a tuple of encoded values.

        Use this method to **encode** the attribute values into a configuration
        """
        attributes = self.attributes

        if len(values) != len(attributes):
            raise Exception("Statespace has %s attributes, thus it can encode lists of values with %s elements. Got list of values %s elements" % (len(attributes), len(attributes), len(values)))

        conf = ()
        for (att,val) in zip(attributes, values):
            conf += (att.encoded_value(val), )

        return conf
