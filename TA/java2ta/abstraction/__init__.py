class AbstractAttribute(object):

    def __init__(self, name, values, initial):
        self.name = name

#        assert isinstance(values, list), "Expected list. Passed '%s'" % values
        assert hasattr(values, "__iter__"), "Expected iterable. Passed '%s'" % values
        assert initial in values, "Expected value in '%s'. Passed: '%s'" % (",".join(values), initial)

        self.domain = {}
        self.rev_domain = {}

        # encode the passed values to form an integer domain; at the same
        # time keep track of the mapping between domain values and abstract value
        for (pos,item) in enumerate(values):
            self.domain[pos] = item

            if item in self.rev_domain:
                raise Exception("You passed a list of values containing twice '%s'. Duplicates are not allowed." % item)

            self.rev_domain[item] = pos

        self.initial = self.rev_domain[initial]

    
    @property
    def encoded_values(self):
        return sorted(self.domain.keys())

    def encoded_value(self, value):
        if value not in self.rev_domain:
            raise ValueError("The passed value ('%s') is not one of the allowed ones . Accepted values: %s" % (value, ",".join(self.values)))
    
        return self.rev_domain[value]

    @property
    def values(self):
        return sorted(self.rev_domain.keys())

    def value(self, encoding):
        if encoding not in self.domain:
            raise ValueError("The passed encoding ('%s') is not one of the allowed ones. Accepted encodings: %s" % (encoding, ",".join(self.encoded_values)))

        return self.domain[encoding]


class StateSpace(object):

    def __init__(self):
        self._attributes = {}
        self._configurations = set([])

    def add_attribute(self, attribute):
        assert isinstance(attribute, AbstractAttribute)
        self._attributes[attribute.name] = attribute
        # invalidate cached configurations
        self._configurations = set([])

    def get_attribute(self, name):
        return self._attributes[name]

    @property
    def attributes(self):
        sort_by_name = lambda att: att.name

        return sorted(self._attributes.values(), key=sort_by_name)
 
    @property   
    def enumerate(self):

        if not self._configurations:
            # enumerate configurations and store for later use
            self._configurations = [()] # start with a single empty configuration
            for attr in self.attributes:
                self._configurations = self.do_multiply(self._configurations, attr)

        return self._configurations

    @property
    def size(self):
        return len(self.enumerate)

    def do_multiply(self, configurations, attribute):
        """     
        Extend each item of the possible configurations by an extra value taken
        from the encoded values of the passed attribute.
        """
        new_configurations = []
        for conf in configurations:

            for val in attribute.encoded_values:
                new_tuple = conf + (val,)
                new_configurations.append(new_tuple)

        return new_configurations

    def value(self, configuration):

        assert isinstance(configuration, tuple)

        if len(configuration) != len(self._attributes):
            raise Exception("Statespace has %s attributes, thus it can decode configurations with %s elements. Got configuration with %s elements" % (len(self._attributes), len(self._attributes), len(configuration)))

        decoded = ()
        for (att,conf_val) in zip(self.attributes, configuration):
            decoded += (att.value(conf_val), )

        return decoded

    def configuration(self, values):
        if len(values) != len(self._attributes):
            raise Exception("Statespace has %s attributes, thus it can encode lists of values with %s elements. Got list of values %s elements" % (len(self._attributes), len(self._attributes), len(values)))

        conf = ()
        for (att,val) in zip(self.attributes, values):
            conf += (att.encoded_value(val), )

        return conf
